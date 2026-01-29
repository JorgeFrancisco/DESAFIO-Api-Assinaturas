package com.globo.api_assinaturas.plan;

import org.springframework.stereotype.Component;

@Component
public class BasicPlanPolicy implements PlanPolicy {

	@Override
	public String code() {
		return "BASIC";
	}

	@Override
	public int maxScreens() {
		return 1;
	}
}