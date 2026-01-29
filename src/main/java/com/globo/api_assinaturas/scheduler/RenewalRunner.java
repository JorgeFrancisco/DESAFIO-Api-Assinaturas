package com.globo.api_assinaturas.scheduler;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.globo.api_assinaturas.config.RenewalProperties;
import com.globo.api_assinaturas.domain.Subscription;
import com.globo.api_assinaturas.domain.enums.SubscriptionStatus;
import com.globo.api_assinaturas.dto.RenewalRunResponse;
import com.globo.api_assinaturas.repository.SubscriptionRepository;
import com.globo.api_assinaturas.service.SubscriptionService;

@Service
public class RenewalRunner {

	private static final Logger log = LoggerFactory.getLogger(RenewalRunner.class);

	private final SubscriptionRepository subRepo;
	private final SubscriptionService subscriptionService;
	private final RenewalProperties props;

	public RenewalRunner(SubscriptionRepository subRepo, SubscriptionService subscriptionService,
			RenewalProperties props) {
		this.subRepo = subRepo;
		this.subscriptionService = subscriptionService;
		this.props = props;
	}

	@Transactional
	public RenewalRunResponse runOnce() {
		Instant startedAt = Instant.now();

		int batch = props.batchSize();
		int maxAttempts = props.maxAttempts();

		long[] retryDelays = props.retryDelaysMinutes();

		log.info("RenewalRunner: locking due subscriptions now={} batch={}", startedAt, batch);

		List<Subscription> due = subRepo.lockDueSubscriptions(startedAt, batch);

		log.info("RenewalRunner: locked {} subscriptions", due.size());

		int processed = 0;
		int renewed = 0;
		int failed = 0;
		int suspended = 0;

		for (Subscription s : due) {
			processed++;

			int beforeFailedAttempts = s.getFailedAttempts();

			SubscriptionStatus beforeStatus = s.getStatus();

			subscriptionService.attemptRenewal(s, maxAttempts, retryDelays);

			if (s.getStatus() == SubscriptionStatus.SUSPENDED && beforeStatus != SubscriptionStatus.SUSPENDED) {
				suspended++;
			} else if (s.getFailedAttempts() > beforeFailedAttempts) {
				failed++;
			} else {
				renewed++;
			}
		}

		Instant finishedAt = Instant.now();

		return new RenewalRunResponse(startedAt, finishedAt, batch, processed, renewed, failed, suspended);
	}
}