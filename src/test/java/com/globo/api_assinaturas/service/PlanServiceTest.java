package com.globo.api_assinaturas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
import com.globo.api_assinaturas.plan.PlanPolicy;
import com.globo.api_assinaturas.plan.PlanPolicyRegistry;
import com.globo.api_assinaturas.repository.PlanRepository;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

	@Mock
	private PlanRepository planRepo;

	@Mock
	private PlanPricingService pricingService;

	@Mock
	private PlanPolicyRegistry policyRegistry;

	@InjectMocks
	private PlanService service;

	@Test
	void list_returnsMappedResponses() {
		var basic = new Plan("BASIC", "Básico", true);
		var premium = new Plan("PREMIUM", "Premium", true);

		when(planRepo.findAll()).thenReturn(List.of(basic, premium));

		var basicPolicy = mock(PlanPolicy.class);

		when(basicPolicy.maxScreens()).thenReturn(1);
		when(policyRegistry.get("BASIC")).thenReturn(basicPolicy);

		var premiumPolicy = mock(PlanPolicy.class);

		when(premiumPolicy.maxScreens()).thenReturn(2);
		when(policyRegistry.get("PREMIUM")).thenReturn(premiumPolicy);

		var t0 = Instant.parse("2026-01-01T00:00:00Z");

		when(pricingService.getCurrentPrice("BASIC"))
				.thenReturn(new PlanPrice(null, basic, BigDecimal.valueOf(19.90), t0, null));
		when(pricingService.getCurrentPrice("PREMIUM"))
				.thenReturn(new PlanPrice(null, premium, BigDecimal.valueOf(39.90), t0, null));

		List<PlanResponse> out = service.list();

		assertEquals(2, out.size());

		var r0 = out.get(0);

		assertEquals("BASIC", r0.code());
		assertEquals("Básico", r0.name());
		assertTrue(r0.active());
		assertEquals(1, r0.maxScreens());
		assertEquals(0, BigDecimal.valueOf(19.90).compareTo(r0.currentPrice()));
		assertEquals(t0, r0.priceValidFrom());

		var r1 = out.get(1);

		assertEquals("PREMIUM", r1.code());
		assertEquals("Premium", r1.name());
		assertTrue(r1.active());
		assertEquals(2, r1.maxScreens());

		verify(planRepo).findAll();
		verify(policyRegistry).get("BASIC");
		verify(policyRegistry).get("PREMIUM");
		verify(pricingService).getCurrentPrice("BASIC");
		verify(pricingService).getCurrentPrice("PREMIUM");
	}

	@Test
	void get_whenFound_returnsResponse() {
		var plan = new Plan("FAMILY", "Família", true);

		when(planRepo.findById("FAMILY")).thenReturn(Optional.of(plan));

		var policy = mock(PlanPolicy.class);

		when(policy.maxScreens()).thenReturn(4);
		when(policyRegistry.get("FAMILY")).thenReturn(policy);

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
		verify(policyRegistry).get("FAMILY");
		verify(pricingService).getCurrentPrice("FAMILY");
	}

	@Test
	void get_whenNotFound_throwsNotFound() {
		when(planRepo.findById("X")).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(NotFoundException.class, () -> service.get("X"));

		assertTrue(ex.getMessage().contains("Plano não encontrado"));

		verify(planRepo).findById("X");
		verifyNoInteractions(policyRegistry);
		verifyNoInteractions(pricingService);
	}
}