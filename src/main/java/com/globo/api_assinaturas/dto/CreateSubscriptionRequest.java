package com.globo.api_assinaturas.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSubscriptionRequest(@NotBlank String planCode) {
}