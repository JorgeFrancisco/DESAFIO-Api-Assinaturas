package com.globo.api_assinaturas.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.globo.api_assinaturas.config.RenewalProperties;
import com.globo.api_assinaturas.domain.Subscription;
import com.globo.api_assinaturas.domain.enums.SubscriptionStatus;
import com.globo.api_assinaturas.dto.RenewalRunResponse;
import com.globo.api_assinaturas.repository.SubscriptionRepository;
import com.globo.api_assinaturas.scheduler.RenewalRunner;
import com.globo.api_assinaturas.service.SubscriptionService;

class RenewalRunnerTest {

	private SubscriptionRepository subRepo;
	private SubscriptionService subscriptionService;
	private RenewalProperties props;
	private RenewalRunner runner;

	@BeforeEach
	void setUp() {
		subRepo = mock(SubscriptionRepository.class);
		subscriptionService = mock(SubscriptionService.class);
		props = new RenewalProperties(50, "600000", new long[] { 120, 360, 720 }, 3);

		runner = new RenewalRunner(subRepo, subscriptionService, props);
	}

	@Test
	void runOnce_whenNoDueSubscriptions_returnsZeros() {
		when(subRepo.lockDueSubscriptions(any(), anyInt())).thenReturn(List.of());

		RenewalRunResponse resp = runner.runOnce();

		assertNotNull(resp);
		assertEquals(50, resp.batchSize());
		assertEquals(0, resp.processed());
		assertEquals(0, resp.renewed());
		assertEquals(0, resp.failed());
		assertEquals(0, resp.suspended());
		assertTrue(resp.finishedAt().compareTo(resp.startedAt()) >= 0);
	}

	@Test
	void runOnce_countsFailed_whenFailedAttemptsIncreased() {
		Subscription s = mock(Subscription.class);

		AtomicInteger failedAttempts = new AtomicInteger(0);
		AtomicReference<SubscriptionStatus> status = new AtomicReference<>(SubscriptionStatus.ACTIVE);

		when(s.getFailedAttempts()).thenAnswer(inv -> failedAttempts.get());
		when(s.getStatus()).thenAnswer(inv -> status.get());

		when(subRepo.lockDueSubscriptions(any(), eq(50))).thenReturn(List.of(s));

		doAnswer(inv -> {
			failedAttempts.incrementAndGet();

			return null;
		}).when(subscriptionService).attemptRenewal(eq(s), eq(3), any(long[].class));

		RenewalRunResponse resp = runner.runOnce();

		assertEquals(1, resp.processed());
		assertEquals(1, resp.failed());
		assertEquals(0, resp.renewed());
		assertEquals(0, resp.suspended());
	}

	@Test
	void runOnce_countsSuspended_whenStatusBecomesSuspended() {
		Subscription s = mock(Subscription.class);

		AtomicInteger failedAttempts = new AtomicInteger(0);
		AtomicReference<SubscriptionStatus> status = new AtomicReference<>(SubscriptionStatus.ACTIVE);

		when(s.getFailedAttempts()).thenAnswer(inv -> failedAttempts.get());
		when(s.getStatus()).thenAnswer(inv -> status.get());

		when(subRepo.lockDueSubscriptions(any(), eq(50))).thenReturn(List.of(s));

		doAnswer(inv -> {
			status.set(SubscriptionStatus.SUSPENDED);
			return null;
		}).when(subscriptionService).attemptRenewal(eq(s), eq(3), any(long[].class));

		RenewalRunResponse resp = runner.runOnce();

		assertEquals(1, resp.processed());
		assertEquals(1, resp.suspended());
		assertEquals(0, resp.failed());
		assertEquals(0, resp.renewed());
	}

	@Test
	void runOnce_countsRenewed_whenNotFailedAndNotSuspended() {
		Subscription s = mock(Subscription.class);

		AtomicInteger failedAttempts = new AtomicInteger(0);
		AtomicReference<SubscriptionStatus> status = new AtomicReference<>(SubscriptionStatus.ACTIVE);

		when(s.getFailedAttempts()).thenAnswer(inv -> failedAttempts.get());
		when(s.getStatus()).thenAnswer(inv -> status.get());

		when(subRepo.lockDueSubscriptions(any(Instant.class), eq(50))).thenReturn(List.of(s));

		doNothing().when(subscriptionService).attemptRenewal(eq(s), eq(3), any(long[].class));

		RenewalRunResponse resp = runner.runOnce();

		assertEquals(1, resp.processed());
		assertEquals(1, resp.renewed());
		assertEquals(0, resp.failed());
		assertEquals(0, resp.suspended());
	}
}