package com.globo.api_assinaturas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.globo.api_assinaturas.domain.Plan;
import com.globo.api_assinaturas.domain.PlanPrice;
import com.globo.api_assinaturas.dto.PlanResponse;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.repository.PlanRepository;

@Service
public class PlanService {

	private final PlanRepository planRepo;
	private final PlanPricingService pricingService;

	public PlanService(PlanRepository planRepo, PlanPricingService pricingService) {
		this.planRepo = planRepo;
		this.pricingService = pricingService;
	}

	@Transactional(readOnly = true)
	public List<PlanResponse> list() {
		var currentPrices = pricingService.getCurrentPricesForActivePlans();

		return planRepo.findAllByActiveTrue().stream().map(plan -> toResponse(plan, currentPrices.get(plan.getCode())))
				.toList();
	}

	@Transactional(readOnly = true)
	public PlanResponse get(String code) {
		Plan plan = planRepo.findById(code).filter(Plan::isActive)
				.orElseThrow(() -> new NotFoundException("Plano não encontrado: " + code));

		var price = pricingService.getCurrentPrice(plan.getCode());

		return toResponse(plan, price);
	}

	private PlanResponse toResponse(Plan plan, PlanPrice price) {
		if (price == null) {
			throw new IllegalStateException("Plano ativo sem preço vigente: " + plan.getCode());
		}

		return new PlanResponse(plan.getCode(), plan.getName(), plan.isActive(), plan.getMaxScreens(), price.getPrice(),
				price.getValidFrom());
	}
}