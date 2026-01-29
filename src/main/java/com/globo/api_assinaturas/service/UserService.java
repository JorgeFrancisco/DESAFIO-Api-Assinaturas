package com.globo.api_assinaturas.service;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.globo.api_assinaturas.domain.AppUser;
import com.globo.api_assinaturas.dto.CreateUserRequest;
import com.globo.api_assinaturas.dto.UserResponse;
import com.globo.api_assinaturas.exceptions.ConflictException;
import com.globo.api_assinaturas.exceptions.NotFoundException;
import com.globo.api_assinaturas.repository.AppUserRepository;

@Service
public class UserService {

	private final AppUserRepository repo;

	public UserService(AppUserRepository repo) {
		this.repo = repo;
	}

	@Transactional
	public UserResponse create(CreateUserRequest req) {
		var user = new AppUser(UUID.randomUUID(), req.name(), req.email().trim().toLowerCase());

		try {
			repo.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new ConflictException("Email já cadastrado: " + req.email());
		}

		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
	}

	@Transactional(readOnly = true)
	public AppUser getEntity(UUID id) {
		return repo.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
	}

	@Transactional(readOnly = true)
	public UserResponse get(UUID id) {
		var u = getEntity(id);

		return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt());
	}
}