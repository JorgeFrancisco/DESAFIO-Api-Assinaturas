package com.globo.api_assinaturas.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.globo.api_assinaturas.enums.SubscriptionStatus;

public record SubscriptionResponse(UUID id, UUID userId, String planCode, SubscriptionStatus status,
		LocalDate startDate, LocalDate expirationDate, boolean hasAccessToday, int failedAttempts, Instant nextRetryAt,
		BigDecimal lastChargedAmount) {
}