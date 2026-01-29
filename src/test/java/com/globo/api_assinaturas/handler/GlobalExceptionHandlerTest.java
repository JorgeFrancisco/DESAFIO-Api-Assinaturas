package com.globo.api_assinaturas.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import com.globo.api_assinaturas.exceptions.BadRequestException;
import com.globo.api_assinaturas.exceptions.ConflictException;
import com.globo.api_assinaturas.exceptions.NotFoundException;

import jakarta.servlet.http.HttpServletRequest;

class GlobalExceptionHandlerTest {

	@Test
	void notFound_setsStatusTitleInstanceAndTimestamp() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();
		HttpServletRequest req = mock(HttpServletRequest.class);

		when(req.getRequestURI()).thenReturn("/apiassinaturas/users/123/subscription");

		ProblemDetail pd = handler.notFound(new NotFoundException("Usuário não encontrado"), req);

		assertEquals(HttpStatus.NOT_FOUND.value(), pd.getStatus());
		assertEquals("Not Found", pd.getTitle());
		assertEquals("Usuário não encontrado", pd.getDetail());
		assertEquals(URI.create("/apiassinaturas/users/123/subscription"), pd.getInstance());
		assertNotNull(pd.getProperties().get("timestamp"));
	}

	@Test
	void conflict_sets409() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();
		HttpServletRequest req = mock(HttpServletRequest.class);

		when(req.getRequestURI()).thenReturn("/x");

		ProblemDetail pd = handler.conflict(new ConflictException("Já existe"), req);

		assertEquals(HttpStatus.CONFLICT.value(), pd.getStatus());
		assertEquals("Já existe", pd.getDetail());
	}

	@Test
	void badRequest_sets400() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();
		HttpServletRequest req = mock(HttpServletRequest.class);

		when(req.getRequestURI()).thenReturn("/x");

		ProblemDetail pd = handler.badRequest(new BadRequestException("inválido"), req);

		assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
		assertEquals("inválido", pd.getDetail());
	}

	@Test
	void generic_sets500() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();
		HttpServletRequest req = mock(HttpServletRequest.class);

		when(req.getRequestURI()).thenReturn("/x");

		ProblemDetail pd = handler.generic(new RuntimeException("boom"), req);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
		assertEquals("Unexpected error", pd.getDetail());
	}
}