package com.globo.api_assinaturas.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.globo.api_assinaturas.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

	Page<AppUser> findByNameContainingIgnoreCase(String name, Pageable pageable);

	Page<AppUser> findByEmailContainingIgnoreCase(String email, Pageable pageable);

	Page<AppUser> findByNameContainingIgnoreCaseAndEmailContainingIgnoreCase(String name, String email,
			Pageable pageable);
}