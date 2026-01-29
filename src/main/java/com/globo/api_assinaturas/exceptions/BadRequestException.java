package com.globo.api_assinaturas.exceptions;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 2569568531564386049L;

	public BadRequestException(String message) {
		super(message);
	}
}