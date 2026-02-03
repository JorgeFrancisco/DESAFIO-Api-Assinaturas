package com.globo.api_assinaturas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.globo.api_assinaturas.domain.Plan;
import com.globo.api_assinaturas.domain.PlanPrice;
import com.globo.api_assinaturas.dto.PlanResponse;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.repository.PlanRepository;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

	@Mock
	private PlanRepository planRepo;

	@Mock
	private PlanPricingService pricingService;

	@InjectMocks
	private PlanService service;

	@Test
	void list_returnsMappedResponses() {
		var basic = new Plan("BASIC", "Básico", true, 1);
		var premium = new Plan("PREMIUM", "Premium", true, 2);

		when(planRepo.findAllByActiveTrue()).thenReturn(List.of(basic, premium));

		var t0 = Instant.parse("2026-01-01T00:00:00Z");

		var basicPrice = new PlanPrice(null, basic, BigDecimal.valueOf(19.90), t0, null);
		var premiumPrice = new PlanPrice(null, premium, BigDecimal.valueOf(39.90), t0, null);

		when(pricingService.getCurrentPricesForActivePlans())
				.thenReturn(Map.of("BASIC", basicPrice, "PREMIUM", premiumPrice));

		List<PlanResponse> out = service.list();

		assertEquals(2, out.size());

		// BASIC
		var r0 = out.get(0);
		assertEquals("BASIC", r0.code());
		assertEquals("Básico", r0.name());
		assertTrue(r0.active());
		assertEquals(1, r0.maxScreens());
		assertEquals(0, BigDecimal.valueOf(19.90).compareTo(r0.currentPrice()));
		assertEquals(t0, r0.priceValidFrom());

		// PREMIUM
		var r1 = out.get(1);
		assertEquals("PREMIUM", r1.code());
		assertEquals("Premium", r1.name());
		assertTrue(r1.active());
		assertEquals(2, r1.maxScreens());
		assertEquals(0, BigDecimal.valueOf(39.90).compareTo(r1.currentPrice()));
		assertEquals(t0, r1.priceValidFrom());

		verify(planRepo).findAllByActiveTrue();
		verify(pricingService).getCurrentPricesForActivePlans();
		verifyNoMoreInteractions(planRepo, pricingService);
	}

	@Test
	void get_whenFound_returnsResponse() {
		var plan = new Plan("FAMILY", "Família", true, 4);

		when(planRepo.findById("FAMILY")).thenReturn(Optional.of(plan));

		var vf = Instant.parse("2026-01-10T00:00:00Z");

		when(pricingService.getCurrentPrice("FAMILY"))
				.thenReturn(new PlanPrice(null, plan, BigDecimal.valueOf(59.90), vf, null));

		PlanResponse out = service.get("FAMILY");

		assertEquals("FAMILY", out.code());
		assertEquals("Família", out.name());
		assertTrue(out.active());
		assertEquals(4, out.maxScreens());
		assertEquals(0, BigDecimal.valueOf(59.90).compareTo(out.currentPrice()));
		assertEquals(vf, out.priceValidFrom());

		verify(planRepo).findById("FAMILY");
		verify(pricingService).getCurrentPrice("FAMILY");
		verifyNoMoreInteractions(planRepo, pricingService);
	}

	@Test
	void get_whenNotFound_throwsNotFound() {
		when(planRepo.findById("X")).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> service.get("X"));

		assertTrue(ex.getMessage().contains("Plano não encontrado"));

		verify(planRepo).findById("X");
		verifyNoInteractions(pricingService);
	}

	@Test
	void get_whenInactive_throwsNotFound() {
		var inactive = new Plan("BASIC", "Básico", false, 1);

		when(planRepo.findById("BASIC")).thenReturn(Optional.of(inactive));

		NotFoundException ex = assertThrows(NotFoundException.class, () -> service.get("BASIC"));

		assertTrue(ex.getMessage().contains("Plano não encontrado"));

		verify(planRepo).findById("BASIC");
		verifyNoInteractions(pricingService);
	}

	@Test
	void list_whenActivePlanHasNoCurrentPrice_throwsIllegalState() {
		var basic = new Plan("BASIC", "Básico", true, 1);
		when(planRepo.findAllByActiveTrue()).thenReturn(List.of(basic));

		when(pricingService.getCurrentPricesForActivePlans()).thenReturn(Map.of());

		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.list());

		assertTrue(ex.getMessage().contains("sem preço vigente"));

		verify(planRepo).findAllByActiveTrue();
		verify(pricingService).getCurrentPricesForActivePlans();
	}
}