package com.globo.api_assinaturas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.globo.api_assinaturas.dto.PlanResponse;
import com.globo.api_assinaturas.dto.UpdatePlanPriceRequest;
import com.globo.api_assinaturas.service.PlanPricingService;
import com.globo.api_assinaturas.service.PlanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/plans")
public class PlanController {

	private final PlanService planService;
	private final PlanPricingService pricingService;

	public PlanController(PlanService planService, PlanPricingService pricingService) {
		this.planService = planService;
		this.pricingService = pricingService;
	}

	@GetMapping
	public List<PlanResponse> list() {
		return planService.list();
	}

	@GetMapping("/{code}")
	public PlanResponse get(@PathVariable String code) {
		return planService.get(code);
	}

	@PutMapping("/{code}/price")
	@ResponseStatus(HttpStatus.OK)
	public PlanResponse updatePrice(@PathVariable String code, @Valid @RequestBody UpdatePlanPriceRequest req) {
		pricingService.updatePrice(code, req.price(), req.validFrom());

		return planService.get(code);
	}
}