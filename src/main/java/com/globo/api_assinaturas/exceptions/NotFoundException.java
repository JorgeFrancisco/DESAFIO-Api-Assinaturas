package com.globo.api_assinaturas.exceptions;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = -6312914930529131432L;

	public NotFoundException(String message) {
		super(message);
	}
}