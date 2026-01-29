package com.globo.api_assinaturas.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.globo.api_assinaturas.dto.CreateSubscriptionRequest;
import com.globo.api_assinaturas.dto.SubscriptionResponse;
import com.globo.api_assinaturas.service.SubscriptionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/{userId}/subscription")
public class SubscriptionController {

	private final SubscriptionService service;

	public SubscriptionController(SubscriptionService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SubscriptionResponse create(@PathVariable UUID userId, @Valid @RequestBody CreateSubscriptionRequest req) {
		return service.create(userId, req);
	}

	@GetMapping
	public SubscriptionResponse getLatest(@PathVariable UUID userId) {
		return service.getLatestForUser(userId);
	}

	@PostMapping("/cancel")
	public SubscriptionResponse cancel(@PathVariable UUID userId) {
		return service.cancel(userId);
	}
}