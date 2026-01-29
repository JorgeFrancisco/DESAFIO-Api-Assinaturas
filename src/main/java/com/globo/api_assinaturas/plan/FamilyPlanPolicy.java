package com.globo.api_assinaturas.plan;

import org.springframework.stereotype.Component;

@Component
public class FamilyPlanPolicy implements PlanPolicy {

	@Override
	public String code() {
		return "FAMILY";
	}

	@Override
	public int maxScreens() {
		return 4;
	}
}