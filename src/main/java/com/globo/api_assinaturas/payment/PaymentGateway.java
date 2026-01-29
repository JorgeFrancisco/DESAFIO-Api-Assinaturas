package com.globo.api_assinaturas.payment;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGateway {
	PaymentResult charge(UUID userId, BigDecimal amount);
}