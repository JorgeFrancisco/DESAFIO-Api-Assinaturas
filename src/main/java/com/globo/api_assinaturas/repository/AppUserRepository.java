package com.globo.api_assinaturas.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.globo.api_assinaturas.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
}