package com.globo.api_assinaturas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.globo.api_assinaturas.domain.Plan;
import com.globo.api_assinaturas.domain.PlanPrice;
import com.globo.api_assinaturas.dto.PlanResponse;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.plan.PlanPolicyRegistry;
import com.globo.api_assinaturas.repository.PlanRepository;

@Service
public class PlanService {

	private final PlanRepository planRepo;
	private final PlanPricingService pricingService;
	private final PlanPolicyRegistry policyRegistry;

	public PlanService(PlanRepository planRepo, PlanPricingService pricingService, PlanPolicyRegistry policyRegistry) {
		this.planRepo = planRepo;
		this.pricingService = pricingService;
		this.policyRegistry = policyRegistry;
	}

	@Transactional(readOnly = true)
	public List<PlanResponse> list() {
		return planRepo.findAll().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public PlanResponse get(String code) {
		Plan plan = planRepo.findById(code).orElseThrow(() -> new NotFoundException("Plano não encontrado: " + code));

		return toResponse(plan);
	}

	private PlanResponse toResponse(Plan plan) {
		var policy = policyRegistry.get(plan.getCode());

		PlanPrice price = pricingService.getCurrentPrice(plan.getCode());

		return new PlanResponse(plan.getCode(), plan.getName(), plan.isActive(), policy.maxScreens(), price.getPrice(),
				price.getValidFrom());
	}
}