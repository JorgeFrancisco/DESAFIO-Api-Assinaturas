package com.globo.api_assinaturas.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RenewalScheduler {

	private static final Logger log = LoggerFactory.getLogger(RenewalScheduler.class);

	private final RenewalRunner runner;

	public RenewalScheduler(RenewalRunner runner) {
		this.runner = runner;
	}

	@Scheduled(fixedDelayString = "${app.renewal.fixed-delay}")
	public void run() {
		var result = runner.runOnce();

		log.info("RenewalScheduler: processed={}, renewed={}, failed={}, suspended={}, batch={}", result.processed(),
				result.renewed(), result.failed(), result.suspended(), result.batchSize());
	}
}
