package com.globo.api_assinaturas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

/**
 * Ajuste aqui se seu usuário não for AppUser.
 */
import com.globo.api_assinaturas.domain.AppUser;
import com.globo.api_assinaturas.domain.Plan;
import com.globo.api_assinaturas.domain.Subscription;
import com.globo.api_assinaturas.dto.CreateSubscriptionRequest;
import com.globo.api_assinaturas.dto.SubscriptionResponse;
import com.globo.api_assinaturas.enums.SubscriptionStatus;
import com.globo.api_assinaturas.exceptions.BadRequestException;
import com.globo.api_assinaturas.exceptions.ConflictException;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.payment.PaymentGateway;
import com.globo.api_assinaturas.payment.PaymentResult;
import com.globo.api_assinaturas.repository.PaymentAttemptRepository;
import com.globo.api_assinaturas.repository.PlanRepository;
import com.globo.api_assinaturas.repository.SubscriptionRepository;

class SubscriptionServiceTest {

	private SubscriptionRepository subRepo;
	private PlanRepository planRepo;
	private UserService userService;
	private PlanPricingService pricingService;
	private PaymentGateway paymentGateway;
	private PaymentAttemptRepository attemptRepo;

	private SubscriptionService service;

	@BeforeEach
	void setUp() {
		subRepo = Mockito.mock(SubscriptionRepository.class);
		planRepo = Mockito.mock(PlanRepository.class);
		userService = Mockito.mock(UserService.class);
		pricingService = Mockito.mock(PlanPricingService.class);
		paymentGateway = Mockito.mock(PaymentGateway.class);
		attemptRepo = Mockito.mock(PaymentAttemptRepository.class);

		service = new SubscriptionService(subRepo, planRepo, userService, pricingService, paymentGateway, attemptRepo);
	}

	@Test
	void create_whenPlanNotFound_throwsNotFound() {
		UUID userId = UUID.randomUUID();
		AppUser user = mock(AppUser.class);

		when(userService.getEntity(userId)).thenReturn(user);
		when(planRepo.findById("BASIC")).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.create(userId, new CreateSubscriptionRequest("BASIC")));
	}

	@Test
	void create_whenPlanInactive_throwsBadRequest() {
		UUID userId = UUID.randomUUID();
		AppUser user = mock(AppUser.class);
		Plan plan = mock(Plan.class);

		when(userService.getEntity(userId)).thenReturn(user);
		when(planRepo.findById("BASIC")).thenReturn(Optional.of(plan));
		when(plan.isActive()).thenReturn(false);
		when(plan.getCode()).thenReturn("BASIC");

		assertThrows(BadRequestException.class, () -> service.create(userId, new CreateSubscriptionRequest("BASIC")));
	}

	@Test
	void create_whenUserAlreadyHasActiveSubscription_throwsConflict() {
		UUID userId = UUID.randomUUID();
		AppUser user = mock(AppUser.class);
		Plan plan = mock(Plan.class);
		Subscription existing = mock(Subscription.class);

		when(userService.getEntity(userId)).thenReturn(user);
		when(planRepo.findById("BASIC")).thenReturn(Optional.of(plan));
		when(plan.isActive()).thenReturn(true);
		when(plan.getCode()).thenReturn("BASIC");
		when(subRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.of(existing));

		assertThrows(ConflictException.class, () -> service.create(userId, new CreateSubscriptionRequest("BASIC")));
		verify(subRepo, never()).save(any());
	}

	@Test
	void create_whenConcurrentInsert_throwsConflict() {
		UUID userId = UUID.randomUUID();
		AppUser user = mock(AppUser.class);
		Plan plan = mock(Plan.class);

		when(userService.getEntity(userId)).thenReturn(user);
		when(planRepo.findById("BASIC")).thenReturn(Optional.of(plan));
		when(plan.isActive()).thenReturn(true);
		when(plan.getCode()).thenReturn("BASIC");
		when(subRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());

		when(subRepo.save(any(Subscription.class))).thenThrow(new DataIntegrityViolationException("partial index"));

		assertThrows(ConflictException.class, () -> service.create(userId, new CreateSubscriptionRequest("BASIC")));
	}

	@Test
	void create_success_returnsResponse() {
		UUID userId = UUID.randomUUID();
		AppUser user = mock(AppUser.class);
		Plan plan = mock(Plan.class);

		when(user.getId()).thenReturn(userId);
		when(plan.getCode()).thenReturn("BASIC");
		when(plan.isActive()).thenReturn(true);

		when(userService.getEntity(userId)).thenReturn(user);
		when(planRepo.findById("BASIC")).thenReturn(Optional.of(plan));
		when(subRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());
		when(subRepo.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

		SubscriptionResponse resp = service.create(userId, new CreateSubscriptionRequest("BASIC"));

		assertNotNull(resp);
		assertEquals(userId, resp.userId());
		assertEquals("BASIC", resp.planCode());
		assertEquals(SubscriptionStatus.ACTIVE, resp.status());
		assertNotNull(resp.startDate());
		assertNotNull(resp.expirationDate());
	}

	@Test
	void getLatestForUser_whenNoSubscriptions_throwsNotFound() {
		UUID userId = UUID.randomUUID();

		when(userService.getEntity(userId)).thenReturn(mock(AppUser.class));
		when(subRepo.findLatestByUserId(eq(userId), any(PageRequest.class))).thenReturn(java.util.List.of());

		assertThrows(NotFoundException.class, () -> service.getLatestForUser(userId));
	}

	@Test
	void getLatestForUser_success_returnsResponse() {
		UUID userId = UUID.randomUUID();
		AppUser user = mock(AppUser.class);
		Plan plan = mock(Plan.class);

		when(user.getId()).thenReturn(userId);
		when(plan.getCode()).thenReturn("PREMIUM");

		Subscription sub = new Subscription(UUID.randomUUID(), user, plan, SubscriptionStatus.ACTIVE, LocalDate.now(),
				LocalDate.now().plusDays(30));

		when(userService.getEntity(userId)).thenReturn(user);
		when(subRepo.findLatestByUserId(eq(userId), any(PageRequest.class))).thenReturn(java.util.List.of(sub));

		SubscriptionResponse resp = service.getLatestForUser(userId);

		assertNotNull(resp);
		assertEquals(userId, resp.userId());
		assertEquals("PREMIUM", resp.planCode());
	}

	@Test
	void cancel_whenNoActiveSubscription_throwsNotFound() {
		UUID userId = UUID.randomUUID();

		when(subRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.cancel(userId));
	}

	@Test
	void cancel_success_cancelsAndSaves() {
		UUID userId = UUID.randomUUID();

		Subscription active = mock(Subscription.class);

		when(subRepo.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.of(active));

		when(subRepo.save(active)).thenReturn(active);

		AppUser user = mock(AppUser.class);
		Plan plan = mock(Plan.class);

		when(active.getId()).thenReturn(UUID.randomUUID());
		when(active.getUser()).thenReturn(user);
		when(active.getPlan()).thenReturn(plan);

		when(user.getId()).thenReturn(userId);
		when(plan.getCode()).thenReturn("BASIC");

		when(active.getStatus()).thenReturn(SubscriptionStatus.CANCELED);
		when(active.getStartDate()).thenReturn(LocalDate.now());
		when(active.getExpirationDate()).thenReturn(LocalDate.now().plusDays(30));
		when(active.getFailedAttempts()).thenReturn(0);
		when(active.getNextRetryAt()).thenReturn(null);
		when(active.getLastChargedAmount()).thenReturn(BigDecimal.ZERO);
		when(active.hasAccess(any(LocalDate.class))).thenReturn(false);

		SubscriptionResponse resp = service.cancel(userId);

		verify(active).cancel();
		verify(subRepo).save(active);

		assertNotNull(resp);
		assertEquals(SubscriptionStatus.CANCELED, resp.status());
	}

	@Test
	void attemptRenewal_whenNotActive_doesNothing() {
		Subscription s = mock(Subscription.class);

		when(s.getStatus()).thenReturn(SubscriptionStatus.CANCELED);

		service.attemptRenewal(s, 3, new long[] { 120, 360, 720 });

		verifyNoInteractions(pricingService, paymentGateway, attemptRepo);
		verify(subRepo, never()).save(any());
	}

	@Test
	void attemptRenewal_success_createsPaymentAttemptAndExtends() {
		Subscription s = mock(Subscription.class);
		AppUser user = mock(AppUser.class);
		Plan plan = mock(Plan.class);

		when(s.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
		when(s.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(UUID.randomUUID());

		when(s.getPlan()).thenReturn(plan);
		when(plan.getCode()).thenReturn("PREMIUM");

		PlanPriceStub price = new PlanPriceStub(BigDecimal.valueOf(39.90));

		when(pricingService.getPriceAt(eq("PREMIUM"), any(Instant.class))).thenReturn(price);

		when(attemptRepo.findMaxAttemptNo(any())).thenReturn(Optional.of(0));

		when(paymentGateway.charge(any(), any())).thenReturn(PaymentResult.ok());

		when(s.getExpirationDate()).thenReturn(LocalDate.of(2026, 2, 27));
		when(s.getId()).thenReturn(UUID.randomUUID());

		service.attemptRenewal(s, 3, new long[] { 120, 360, 720 });

		verify(s).markAttemptedNow(any());
		verify(s).markPaidAndExtend(eq(LocalDate.of(2026, 3, 29)), eq(BigDecimal.valueOf(39.90)), any());
		verify(attemptRepo).save(any());
		verify(subRepo).save(s);
	}

	@Test
	void attemptRenewal_fail_schedulesRetry() {
		Subscription s = mock(Subscription.class);
		AppUser user = mock(AppUser.class);
		Plan plan = mock(Plan.class);

		when(s.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
		when(s.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(UUID.randomUUID());
		when(s.getPlan()).thenReturn(plan);
		when(plan.getCode()).thenReturn("PREMIUM");

		when(s.getId()).thenReturn(UUID.randomUUID());
		when(s.getFailedAttempts()).thenReturn(0);

		PlanPriceStub price = new PlanPriceStub(BigDecimal.valueOf(39.90));

		when(pricingService.getPriceAt(eq("PREMIUM"), any(Instant.class))).thenReturn(price);

		when(attemptRepo.findMaxAttemptNo(any())).thenReturn(Optional.of(0));
		when(paymentGateway.charge(any(), any())).thenReturn(PaymentResult.fail("recusado"));

		service.attemptRenewal(s, 3, new long[] { 120, 360, 720 });

		verify(attemptRepo).save(any());
		verify(s).markFailedAttempt(eq(1), any(Instant.class), any(Instant.class));
		verify(subRepo).save(s);
	}

	@Test
	void attemptRenewal_fail_reachesMaxAttempts_suspends() {
		Subscription s = mock(Subscription.class);
		AppUser user = mock(AppUser.class);
		Plan plan = mock(Plan.class);

		when(s.getStatus()).thenReturn(SubscriptionStatus.ACTIVE);
		when(s.getUser()).thenReturn(user);
		when(user.getId()).thenReturn(UUID.randomUUID());
		when(s.getPlan()).thenReturn(plan);
		when(plan.getCode()).thenReturn("PREMIUM");

		when(s.getId()).thenReturn(UUID.randomUUID());
		when(s.getFailedAttempts()).thenReturn(2); // já tinha 2

		PlanPriceStub price = new PlanPriceStub(BigDecimal.valueOf(39.90));

		when(pricingService.getPriceAt(eq("PREMIUM"), any(Instant.class))).thenReturn(price);

		when(attemptRepo.findMaxAttemptNo(any())).thenReturn(Optional.of(2));
		when(paymentGateway.charge(any(), any())).thenReturn(PaymentResult.fail("recusado"));

		service.attemptRenewal(s, 3, new long[] { 120, 360, 720 });

		verify(s).suspend();
		verify(subRepo).save(s);
	}

	private static final class PlanPriceStub extends com.globo.api_assinaturas.domain.PlanPrice {
		private final BigDecimal price;

		PlanPriceStub(BigDecimal price) {
			super(null, null, price, Instant.EPOCH, null);
			this.price = price;
		}

		@Override
		public BigDecimal getPrice() {
			return price;
		}
	}
}