package com.globo.api_assinaturas.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PlanResponse(String code, String name, boolean active, int maxScreens, BigDecimal currentPrice,
		Instant priceValidFrom) {
}