package com.globo.api_assinaturas.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Gateway fake para o desafio.
 */
@Component
public class FakePaymentGateway implements PaymentGateway {

	private final Random random = new Random();
	private final double failRate;

	public FakePaymentGateway(@Value("${app.payment.fail-rate:0.25}") double failRate) {
		this.failRate = failRate;
	}

	@Override
	public PaymentResult charge(UUID userId, BigDecimal amount) {
		// Falha aleatoria controlada por configuracao
		boolean fail = random.nextDouble() < failRate;

		if (fail) {
			return PaymentResult.fail("Pagamento recusado (simulado)");
		}

		return PaymentResult.ok();
	}
}