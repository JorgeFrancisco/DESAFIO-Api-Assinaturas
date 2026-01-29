package com.globo.api_assinaturas.schedule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.globo.api_assinaturas.dto.RenewalRunResponse;
import com.globo.api_assinaturas.scheduler.RenewalRunner;
import com.globo.api_assinaturas.scheduler.RenewalScheduler;

class RenewalSchedulerTest {

	private RenewalRunner runner;

	private RenewalScheduler scheduler;

	@BeforeEach
	void setUp() {
		runner = mock(RenewalRunner.class);

		scheduler = new RenewalScheduler(runner);
	}

	@Test
	void run_callsRunnerOnce() {
		when(runner.runOnce()).thenReturn(new RenewalRunResponse(null, null, 50, 0, 0, 0, 0));

		scheduler.run();

		verify(runner).runOnce();
	}
}