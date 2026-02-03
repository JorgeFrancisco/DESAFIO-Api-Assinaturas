package com.globo.api_assinaturas.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.globo.api_assinaturas.domain.Plan;
import com.globo.api_assinaturas.domain.PlanPrice;
import com.globo.api_assinaturas.exceptions.BadRequestException;
import com.globo.api_assinaturas.exceptions.ConflictException;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.repository.PlanPriceRepository;
import com.globo.api_assinaturas.repository.PlanRepository;

@Service
public class PlanPricingService {

	private final PlanRepository planRepo;
	private final PlanPriceRepository priceRepo;

	public PlanPricingService(PlanRepository planRepo, PlanPriceRepository priceRepo) {
		this.planRepo = planRepo;
		this.priceRepo = priceRepo;
	}

	@Transactional(readOnly = true)
	public PlanPrice getPriceAt(String planCode, Instant at) {
		return priceRepo.findPriceAt(planCode, at)
				.orElseThrow(() -> new NotFoundException("Preço não encontrado para plano " + planCode + " em " + at));
	}

	@Transactional(readOnly = true)
	public PlanPrice getCurrentPrice(String planCode) {
		return priceRepo.findCurrent(planCode)
				.orElseThrow(() -> new NotFoundException("Preço vigente não encontrado para plano " + planCode));
	}

	@Transactional(readOnly = true)
	public Map<String, PlanPrice> getCurrentPricesForActivePlans() {
		return priceRepo.findCurrentPricesForActivePlans().stream()
				.collect(Collectors.toUnmodifiableMap(pp -> pp.getPlan().getCode(), Function.identity()));
	}

	/**
	 * Atualiza o preço do plano criando uma nova vigência e encerrando a anterior.
	 *
	 * Regras: - validFrom não pode ser anterior ao validFrom vigente atual - Se o
	 * preço e a vigência já forem iguais ao vigente atual, nenhuma alteração é
	 * feita
	 *
	 * Implementação: - Encerra o preço vigente atual definindo validTo = validFrom
	 * - Cria um novo preço vigente (validTo = null) - Evita recriação desnecessária
	 * quando não há mudança efetiva - O banco garante unicidade do preço vigente
	 * via constraint (valid_to is null)
	 */
	@Transactional
	public PlanPrice updatePrice(String planCode, BigDecimal newPrice, Instant validFrom) {
		Plan plan = planRepo.findById(planCode)
				.orElseThrow(() -> new NotFoundException("Plano não encontrado: " + planCode));

		PlanPrice current = getCurrentPrice(planCode);

		if (validFrom.isBefore(current.getValidFrom())) {
			throw new BadRequestException("validFrom deve ser >= validFrom atual (" + current.getValidFrom() + ")");
		}

		// Se o preço vigente já possui o mesmo valor e a mesma data de início,
		// não há alteração de estado a ser feita.
		// Retornamos o registro atual para evitar recriação desnecessária.
		if (validFrom.equals(current.getValidFrom()) && current.getPrice().compareTo(newPrice) == 0) {
			return current;
		}

		// Encerra a vigência atual exatamente no instante em que a nova começa
		current.setValidTo(validFrom);

		priceRepo.saveAndFlush(current);

		// Cria o novo preço vigente (validTo = null)
		PlanPrice next = new PlanPrice(UUID.randomUUID(), plan, newPrice, validFrom, null);

		try {
			return priceRepo.save(next);
		} catch (DataIntegrityViolationException e) {
			// Corrida: outro request pode ter inserido um "vigente" ao mesmo tempo
			// (valid_to = null)
			throw new ConflictException("Já existe um preço vigente para este plano (concorrência). Tente novamente.");
		}
	}
}