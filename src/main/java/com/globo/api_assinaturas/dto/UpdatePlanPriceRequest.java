package com.globo.api_assinaturas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdatePlanPriceRequest(@NotNull @DecimalMin("0.0") BigDecimal price, @NotNull Instant validFrom) {
}