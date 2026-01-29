package com.globo.api_assinaturas.handler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.globo.api_assinaturas.exceptions.BadRequestException;
import com.globo.api_assinaturas.exceptions.ConflictException;
import com.globo.api_assinaturas.exceptions.NotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
		return problem(HttpStatus.CONFLICT, "Operação violou uma restrição de integridade", req.getRequestURI(), null);
	}

	@ExceptionHandler(NotFoundException.class)
	public ProblemDetail notFound(NotFoundException ex, HttpServletRequest req) {
		return problem(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI(), null);
	}

	@ExceptionHandler(ConflictException.class)
	public ProblemDetail conflict(ConflictException ex, HttpServletRequest req) {
		return problem(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI(), null);
	}

	@ExceptionHandler(BadRequestException.class)
	public ProblemDetail badRequest(BadRequestException ex, HttpServletRequest req) {
		return problem(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI(), null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		Map<String, String> errors = new HashMap<>();

		for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
			errors.put(fe.getField(), fe.getDefaultMessage());
		}

		return problem(HttpStatus.BAD_REQUEST, "Validation error", req.getRequestURI(), errors);
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail generic(Exception ex, HttpServletRequest req) {
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req.getRequestURI(), null);
	}

	private ProblemDetail problem(HttpStatus status, String detail, String path, Map<String, String> errors) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);

		pd.setType(URI.create("about:blank"));
		pd.setTitle(status.getReasonPhrase());
		pd.setInstance(URI.create(path));
		pd.setProperty("timestamp", Instant.now());

		if (errors != null && !errors.isEmpty()) {
			pd.setProperty("errors", errors);
		}

		return pd;
	}
}