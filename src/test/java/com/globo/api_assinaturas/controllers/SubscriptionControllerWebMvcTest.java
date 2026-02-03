package com.globo.api_assinaturas.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.globo.api_assinaturas.controller.SubscriptionController;
import com.globo.api_assinaturas.dto.SubscriptionResponse;
import com.globo.api_assinaturas.enums.SubscriptionStatus;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.handler.GlobalExceptionHandler;
import com.globo.api_assinaturas.service.SubscriptionService;

@WebMvcTest(controllers = { SubscriptionController.class, GlobalExceptionHandler.class })
class SubscriptionControllerWebMvcTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private SubscriptionService service;

	@Test
	void getLatest_whenNotFound_returns404ProblemDetail() throws Exception {
		UUID userId = UUID.randomUUID();

		when(service.getLatestForUser(userId)).thenThrow(new NotFoundException("Usuário não possui assinaturas"));

		mvc.perform(get("/users/{userId}/subscription", userId).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Usuário não possui assinaturas"))
				.andExpect(jsonPath("$.title").value("Not Found"))
				.andExpect(jsonPath("$.instance").value("/users/" + userId + "/subscription"));
	}

	@Test
	void create_returns201() throws Exception {
		UUID userId = UUID.randomUUID();

		SubscriptionResponse resp = new SubscriptionResponse(UUID.randomUUID(), userId, "BASIC",
				SubscriptionStatus.ACTIVE, java.time.LocalDate.now(), java.time.LocalDate.now().plusDays(30), true, 0,
				null, java.math.BigDecimal.ZERO);

		when(service.create(eq(userId), any())).thenReturn(resp);

		mvc.perform(post("/users/{userId}/subscription", userId).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content("""
						{"planCode":"BASIC"}
						""")).andExpect(status().isCreated()).andExpect(jsonPath("$.userId").value(userId.toString()))
				.andExpect(jsonPath("$.planCode").value("BASIC"));
	}
}
