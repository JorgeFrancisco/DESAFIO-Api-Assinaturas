package com.globo.api_assinaturas.payment;

public record PaymentResult(boolean success, String errorMessage) {

	public static PaymentResult ok() {
		return new PaymentResult(true, null);
	}

	public static PaymentResult fail(String message) {
		return new PaymentResult(false, message);
	}
}