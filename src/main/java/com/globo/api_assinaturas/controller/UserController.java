package com.globo.api_assinaturas.controller;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.globo.api_assinaturas.dto.CreateUserRequest;
import com.globo.api_assinaturas.dto.PageResponse;
import com.globo.api_assinaturas.dto.UserResponse;
import com.globo.api_assinaturas.enums.SortDirection;
import com.globo.api_assinaturas.enums.UserSortBy;
import com.globo.api_assinaturas.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

	private static final int MAX_PAGE_SIZE = 20;

	private final UserService service;

	public UserController(UserService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
		return service.create(req);
	}

	@GetMapping("/{id}")
	public UserResponse get(@PathVariable UUID id) {
		return service.get(id);
	}

	@GetMapping
	public PageResponse<UserResponse> search(@RequestParam(required = false) String name,
			@RequestParam(required = false) String email, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "NAME") UserSortBy sortBy,
			@RequestParam(defaultValue = "ASC") SortDirection direction) {
		int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

		var sort = direction == SortDirection.DESC ? Sort.by(sortBy.property()).descending()
				: Sort.by(sortBy.property()).ascending();

		var pageable = PageRequest.of(Math.max(page, 0), safeSize, sort);

		return PageResponse.from(service.search(name, email, pageable));
	}
}