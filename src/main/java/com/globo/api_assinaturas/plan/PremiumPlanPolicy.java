package com.globo.api_assinaturas.plan;

import org.springframework.stereotype.Component;

@Component
public class PremiumPlanPolicy implements PlanPolicy {

	@Override
	public String code() {
		return "PREMIUM";
	}

	@Override
	public int maxScreens() {
		return 2;
	}
}