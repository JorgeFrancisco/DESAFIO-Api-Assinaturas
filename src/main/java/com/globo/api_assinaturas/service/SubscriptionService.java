package com.globo.api_assinaturas.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.globo.api_assinaturas.domain.PaymentAttempt;
import com.globo.api_assinaturas.domain.Subscription;
import com.globo.api_assinaturas.dto.CreateSubscriptionRequest;
import com.globo.api_assinaturas.dto.SubscriptionResponse;
import com.globo.api_assinaturas.enums.PaymentAttemptStatus;
import com.globo.api_assinaturas.enums.SubscriptionStatus;
import com.globo.api_assinaturas.exceptions.BadRequestException;
import com.globo.api_assinaturas.exceptions.ConflictException;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.payment.PaymentGateway;
import com.globo.api_assinaturas.repository.PaymentAttemptRepository;
import com.globo.api_assinaturas.repository.PlanRepository;
import com.globo.api_assinaturas.repository.SubscriptionRepository;

@Service
public class SubscriptionService {

	private final SubscriptionRepository subRepo;
	private final PlanRepository planRepo;
	private final UserService userService;
	private final PlanPricingService pricingService;
	private final PaymentGateway paymentGateway;
	private final PaymentAttemptRepository attemptRepo;

	public SubscriptionService(SubscriptionRepository subRepo, PlanRepository planRepo, UserService userService,
			PlanPricingService pricingService, PaymentGateway paymentGateway, PaymentAttemptRepository attemptRepo) {
		this.subRepo = subRepo;
		this.planRepo = planRepo;
		this.userService = userService;
		this.pricingService = pricingService;
		this.paymentGateway = paymentGateway;
		this.attemptRepo = attemptRepo;
	}

	@Transactional
	public SubscriptionResponse create(UUID userId, CreateSubscriptionRequest req) {
		var user = userService.getEntity(userId);

		var plan = planRepo.findById(req.planCode())
				.orElseThrow(() -> new NotFoundException("Plano não encontrado: " + req.planCode()));

		if (!plan.isActive()) {
			throw new BadRequestException("Plano inativo: " + plan.getCode());
		}

		// Check lógico (o banco também garante via índice parcial)
		subRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).ifPresent(s -> {
			throw new ConflictException("Usuário já possui assinatura ATIVA");
		});

		LocalDate today = LocalDate.now();
		LocalDate expiration = today.plusDays(30);

		var sub = new Subscription(UUID.randomUUID(), user, plan, SubscriptionStatus.ACTIVE, today, expiration);

		try {
			subRepo.save(sub);
		} catch (DataIntegrityViolationException e) {
			throw new ConflictException("Usuário já possui assinatura ATIVA (concorrência).");
		}

		return toResponse(sub);
	}

	@Transactional(readOnly = true)
	public SubscriptionResponse getLatestForUser(UUID userId) {
		userService.getEntity(userId);

		var list = subRepo.findLatestByUserId(userId, PageRequest.of(0, 1));

		return list.stream().findFirst().map(this::toResponse)
				.orElseThrow(() -> new NotFoundException("Usuário não possui assinaturas"));
	}

	@Transactional
	public SubscriptionResponse cancel(UUID userId) {
		var active = subRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
				.orElseThrow(() -> new NotFoundException("Assinatura ATIVA não encontrada para usuário: " + userId));

		active.cancel();

		subRepo.save(active);

		return toResponse(active);
	}

	public SubscriptionResponse toResponse(Subscription s) {
		LocalDate today = LocalDate.now();

		return new SubscriptionResponse(s.getId(), s.getUser().getId(), s.getPlan().getCode(), s.getStatus(),
				s.getStartDate(), s.getExpirationDate(), s.hasAccess(today), s.getFailedAttempts(), s.getNextRetryAt(),
				s.getLastChargedAmount());
	}

	/**
	 * Executa uma tentativa de renovação para uma assinatura já lockada pelo
	 * scheduler.
	 */
	@Transactional
	public void attemptRenewal(Subscription s, int maxAttempts, long[] retryDelaysMinutes) {
		if (s.getStatus() != SubscriptionStatus.ACTIVE) {
			return;
		}

		Instant now = Instant.now();

		s.markAttemptedNow(now);

		// preço vigente no momento da tentativa
		BigDecimal amount = pricingService.getPriceAt(s.getPlan().getCode(), now).getPrice();

		int nextAttemptNo = attemptRepo.findMaxAttemptNo(s.getId()).orElse(0) + 1;

		var result = paymentGateway.charge(s.getUser().getId(), amount);

		if (result.success()) {
			// Extende 30 dias a partir do vencimento atual (mantém ciclo)
			LocalDate base = s.getExpirationDate();
			LocalDate newExpiration = base.plusDays(30);

			s.markPaidAndExtend(newExpiration, amount, now);

			attemptRepo.save(new PaymentAttempt(UUID.randomUUID(), s, nextAttemptNo, PaymentAttemptStatus.SUCCESS, null,
					amount));

			subRepo.save(s);

			return;
		}

		// Falha
		attemptRepo.save(new PaymentAttempt(UUID.randomUUID(), s, nextAttemptNo, PaymentAttemptStatus.FAILED,
				result.errorMessage(), amount));

		int failed = s.getFailedAttempts() + 1;
		if (failed >= maxAttempts) {
			s.suspend();

			subRepo.save(s);

			return;
		}

		long delayMin = retryDelaysMinutes[Math.min(failed - 1, retryDelaysMinutes.length - 1)];

		Instant nextRetry = now.plusSeconds(delayMin * 60L);

		s.markFailedAttempt(failed, nextRetry, now);

		subRepo.save(s);
	}
}