package com.globo.api_assinaturas.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, Instant createdAt) {
}