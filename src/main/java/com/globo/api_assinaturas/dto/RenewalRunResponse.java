package com.globo.api_assinaturas.dto;

import java.time.Instant;

public record RenewalRunResponse(Instant startedAt, Instant finishedAt, int batchSize, int processed, int renewed,
		int failed, int suspended) {
}