package com.globo.api_assinaturas.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.globo.api_assinaturas.controller.AdminRenewalController;
import com.globo.api_assinaturas.dto.RenewalRunResponse;
import com.globo.api_assinaturas.scheduler.RenewalRunner;

@WebMvcTest(controllers = AdminRenewalController.class)
@ActiveProfiles("test")
class AdminRenewalControllerWebMvcTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private RenewalRunner runner;

	@Test
	void runOnce_returns200AndBody() throws Exception {
		when(runner.runOnce()).thenReturn(new RenewalRunResponse(Instant.parse("2026-01-01T00:00:00Z"),
				Instant.parse("2026-01-01T00:00:01Z"), 50, 1, 1, 0, 0));

		mvc.perform(post("/admin/renewals/run").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.batchSize").value(50)).andExpect(jsonPath("$.processed").value(1));
	}
}