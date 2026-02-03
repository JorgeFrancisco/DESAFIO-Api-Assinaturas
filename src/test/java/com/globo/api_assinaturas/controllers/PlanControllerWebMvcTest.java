package com.globo.api_assinaturas.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.globo.api_assinaturas.controller.PlanController;
import com.globo.api_assinaturas.dto.PlanResponse;
import com.globo.api_assinaturas.exceptions.BadRequestException;
import com.globo.api_assinaturas.exceptions.ConflictException;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.handler.GlobalExceptionHandler;
import com.globo.api_assinaturas.service.PlanPricingService;
import com.globo.api_assinaturas.service.PlanService;

@WebMvcTest(controllers = { PlanController.class, GlobalExceptionHandler.class })
class PlanControllerWebMvcTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private PlanService planService;

	@MockitoBean
	private PlanPricingService pricingService;

	@Test
	void list_returns200AndArray() throws Exception {
		when(planService.list()).thenReturn(List.of(
				new PlanResponse("BASIC", "Básico", true, 1, BigDecimal.valueOf(19.9),
						Instant.parse("2026-01-01T00:00:00Z")),
				new PlanResponse("PREMIUM", "Premium", true, 2, BigDecimal.valueOf(39.9),
						Instant.parse("2026-01-01T00:00:00Z"))));

		mvc.perform(get("/plans").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].code").value("BASIC")).andExpect(jsonPath("$[1].code").value("PREMIUM"))
				.andExpect(jsonPath("$[0].maxScreens").value(1)).andExpect(jsonPath("$[1].maxScreens").value(2));
	}

	@Test
	void get_returns200() throws Exception {
		when(planService.get("BASIC")).thenReturn(new PlanResponse("BASIC", "Básico", true, 1, BigDecimal.valueOf(19.9),
				Instant.parse("2026-01-01T00:00:00Z")));

		mvc.perform(get("/plans/{code}", "BASIC").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("BASIC")).andExpect(jsonPath("$.name").value("Básico"))
				.andExpect(jsonPath("$.maxScreens").value(1));
	}

	@Test
	void updatePrice_returns200AndUpdatedPlan() throws Exception {
		when(planService.get("BASIC")).thenReturn(new PlanResponse("BASIC", "Básico", true, 1, BigDecimal.valueOf(29.9),
				Instant.parse("2026-02-01T00:00:00Z")));

		mvc.perform(put("/plans/{code}/price", "BASIC").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content("""
						{
						  "price": 29.9,
						  "validFrom": "2026-02-01T00:00:00Z"
						}
						""")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value("BASIC"))
				.andExpect(jsonPath("$.currentPrice").value(29.9))
				.andExpect(jsonPath("$.priceValidFrom").value("2026-02-01T00:00:00Z"))
				.andExpect(jsonPath("$.maxScreens").value(1));
	}

	@Test
	void updatePrice_whenBodyInvalid_returns400ProblemDetail() throws Exception {
		mvc.perform(put("/plans/{code}/price", "BASIC").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content("""
						{}
						""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.detail").value("Validation error"))
				.andExpect(jsonPath("$.instance").value("/plans/BASIC/price")).andExpect(jsonPath("$.errors").exists());
	}

	@Test
	void updatePrice_whenPlanNotFound_returns404ProblemDetail() throws Exception {
		doThrow(new NotFoundException("Plano não encontrado")).when(pricingService).updatePrice(eq("BASIC"), any(),
				any());

		mvc.perform(put("/plans/{code}/price", "BASIC").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content("""
						{
						  "price": 29.9,
						  "validFrom": "2026-02-01T00:00:00Z"
						}
						""")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Plano não encontrado"))
				.andExpect(jsonPath("$.instance").value("/plans/BASIC/price"));
	}

	@Test
	void updatePrice_whenBadRequest_returns400ProblemDetail() throws Exception {
		doThrow(new BadRequestException("validFrom inválido")).when(pricingService).updatePrice(eq("BASIC"), any(),
				any());

		mvc.perform(put("/plans/{code}/price", "BASIC").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content("""
						{
						  "price": 29.9,
						  "validFrom": "2026-02-01T00:00:00Z"
						}
						""")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("validFrom inválido"));
	}

	@Test
	void updatePrice_whenConflict_returns409ProblemDetail() throws Exception {
		doThrow(new ConflictException("concorrência")).when(pricingService).updatePrice(eq("BASIC"), any(), any());

		mvc.perform(put("/plans/{code}/price", "BASIC").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content("""
						{
						  "price": 29.9,
						  "validFrom": "2026-02-01T00:00:00Z"
						}
						""")).andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value("concorrência"));
	}
}