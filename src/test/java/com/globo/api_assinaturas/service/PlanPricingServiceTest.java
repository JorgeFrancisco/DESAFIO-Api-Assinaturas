package com.globo.api_assinaturas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import com.globo.api_assinaturas.domain.Plan;
import com.globo.api_assinaturas.domain.PlanPrice;
import com.globo.api_assinaturas.exceptions.BadRequestException;
import com.globo.api_assinaturas.exceptions.ConflictException;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.repository.PlanPriceRepository;
import com.globo.api_assinaturas.repository.PlanRepository;

class PlanPricingServiceTest {

	private PlanRepository planRepo;
	private PlanPriceRepository priceRepo;
	private PlanPricingService service;

	@BeforeEach
	void setUp() {
		planRepo = Mockito.mock(PlanRepository.class);
		priceRepo = Mockito.mock(PlanPriceRepository.class);
		service = new PlanPricingService(planRepo, priceRepo);
	}

	@Test
	void getPriceAt_whenNotFound_throwsNotFound() {
		Instant at = Instant.parse("2026-01-01T00:00:00Z");

		when(priceRepo.findPriceAt("BASIC", at)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getPriceAt("BASIC", at));

		assertTrue(ex.getMessage().contains("Preço não encontrado"));
	}

	@Test
	void getCurrentPrice_whenNotFound_throwsNotFound() {
		when(priceRepo.findCurrent("BASIC")).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getCurrentPrice("BASIC"));

		assertTrue(ex.getMessage().contains("Preço vigente não encontrado"));
	}

	@Test
	void updatePrice_whenPlanNotFound_throwsNotFound() {
		when(planRepo.findById("BASIC")).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> service.updatePrice("BASIC", BigDecimal.valueOf(29.90), Instant.parse("2026-02-01T00:00:00Z")));
		verifyNoInteractions(priceRepo);
	}

	@Test
	void updatePrice_whenValidFromBeforeCurrentValidFrom_throwsBadRequest() {
		Plan plan = mock(Plan.class);
		PlanPrice current = new PlanPrice(UUID.randomUUID(), plan, BigDecimal.valueOf(19.90),
				Instant.parse("2026-02-01T00:00:00Z"), null);

		when(planRepo.findById("BASIC")).thenReturn(Optional.of(plan));
		when(priceRepo.findCurrent("BASIC")).thenReturn(Optional.of(current));

		Instant invalid = Instant.parse("2026-01-31T23:59:59Z");

		BadRequestException ex = assertThrows(BadRequestException.class,
				() -> service.updatePrice("BASIC", BigDecimal.valueOf(29.90), invalid));

		assertTrue(ex.getMessage().contains("validFrom deve ser >="));
		verify(priceRepo, never()).saveAndFlush(any());
		verify(priceRepo, never()).save(any(PlanPrice.class));
	}

	@Test
	void updatePrice_whenSameValidFromAndSamePrice_returnsCurrent_withoutWriting() {
		Plan plan = mock(Plan.class);
		Instant vf = Instant.parse("2026-02-01T00:00:00Z");
		BigDecimal price = BigDecimal.valueOf(19.90);

		PlanPrice current = new PlanPrice(UUID.randomUUID(), plan, price, vf, null);

		when(planRepo.findById("BASIC")).thenReturn(Optional.of(plan));
		when(priceRepo.findCurrent("BASIC")).thenReturn(Optional.of(current));

		PlanPrice result = service.updatePrice("BASIC", BigDecimal.valueOf(19.90), vf);

		assertSame(current, result);
		verify(priceRepo, never()).saveAndFlush(any());
		verify(priceRepo, never()).save(any(PlanPrice.class));
	}

	@Test
	void updatePrice_success_closesCurrent_andCreatesNewCurrent() {
		Plan plan = mock(Plan.class);
		Instant currentFrom = Instant.parse("2026-01-01T00:00:00Z");
		PlanPrice current = new PlanPrice(UUID.randomUUID(), plan, BigDecimal.valueOf(19.90), currentFrom, null);

		when(planRepo.findById("BASIC")).thenReturn(Optional.of(plan));
		when(priceRepo.findCurrent("BASIC")).thenReturn(Optional.of(current));

		Instant newFrom = Instant.parse("2026-02-01T00:00:00Z");
		BigDecimal newPrice = BigDecimal.valueOf(29.90);

		ArgumentCaptor<PlanPrice> savedCaptor = ArgumentCaptor.forClass(PlanPrice.class);

		when(priceRepo.saveAndFlush(any(PlanPrice.class))).thenAnswer(inv -> inv.getArgument(0));
		when(priceRepo.save(any(PlanPrice.class))).thenAnswer(inv -> inv.getArgument(0));

		PlanPrice next = service.updatePrice("BASIC", newPrice, newFrom);

		assertNotNull(next);
		assertEquals(newFrom, next.getValidFrom());
		assertNull(next.getValidTo());
		assertEquals(0, next.getPrice().compareTo(newPrice));

		verify(priceRepo).saveAndFlush(savedCaptor.capture());

		PlanPrice closed = savedCaptor.getValue();

		assertEquals(newFrom, closed.getValidTo(),
				"O preço atual deve ser fechado em validTo = validFrom do novo preço");

		verify(priceRepo).save(any(PlanPrice.class));
	}

	@Test
	void updatePrice_whenRaceConditionOnPartialIndex_throwsConflict() {
		Plan plan = mock(Plan.class);
		Instant currentFrom = Instant.parse("2026-01-01T00:00:00Z");
		PlanPrice current = new PlanPrice(UUID.randomUUID(), plan, BigDecimal.valueOf(19.90), currentFrom, null);

		when(planRepo.findById("BASIC")).thenReturn(Optional.of(plan));
		when(priceRepo.findCurrent("BASIC")).thenReturn(Optional.of(current));

		when(priceRepo.saveAndFlush(any(PlanPrice.class))).thenAnswer(inv -> inv.getArgument(0));
		when(priceRepo.save(any(PlanPrice.class)))
				.thenThrow(new DataIntegrityViolationException("ux_plan_price_current"));

		assertThrows(ConflictException.class,
				() -> service.updatePrice("BASIC", BigDecimal.valueOf(29.90), Instant.parse("2026-02-01T00:00:00Z")));
	}
}