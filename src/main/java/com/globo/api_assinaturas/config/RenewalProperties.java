package com.globo.api_assinaturas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.renewal")
public record RenewalProperties(int batchSize, String fixedDelay, long[] retryDelaysMinutes, int maxAttempts) {

	public RenewalProperties {
		if (maxAttempts <= 0) {
			maxAttempts = 3;
		}

		if (batchSize <= 0) {
			batchSize = 50;
		}

		if (retryDelaysMinutes == null || retryDelaysMinutes.length == 0) {
			retryDelaysMinutes = new long[] { 120, 360, 720 };
		}

		if (fixedDelay == null || fixedDelay.isBlank()) {
			fixedDelay = "600000";
		}
	}
}