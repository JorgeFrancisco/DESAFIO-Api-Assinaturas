package com.globo.api_assinaturas.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.globo.api_assinaturas.domain.Plan;

public interface PlanRepository extends JpaRepository<Plan, String> {
}