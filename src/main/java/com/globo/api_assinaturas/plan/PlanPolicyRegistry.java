package com.globo.api_assinaturas.plan;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.globo.api_assinaturas.exceptions.BadRequestException;

@Component
public class PlanPolicyRegistry {

	private final Map<String, PlanPolicy> policiesByCode;

	public PlanPolicyRegistry(List<PlanPolicy> policies) {
		this.policiesByCode = policies.stream()
				.collect(Collectors.toUnmodifiableMap(PlanPolicy::code, Function.identity()));
	}

	public PlanPolicy get(String code) {
		PlanPolicy p = policiesByCode.get(code);

		if (p == null) {
			throw new BadRequestException("Plano desconhecido: " + code);
		}

		return p;
	}
}