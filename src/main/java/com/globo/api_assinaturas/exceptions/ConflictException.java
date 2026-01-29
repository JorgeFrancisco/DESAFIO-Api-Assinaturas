package com.globo.api_assinaturas.exceptions;

public class ConflictException extends RuntimeException {

	private static final long serialVersionUID = 7760106701185505299L;

	public ConflictException(String message) {
		super(message);
	}
}