package com.globo.api_assinaturas.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.globo.api_assinaturas.domain.PaymentAttempt;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {

	@Query("""
			select max(pa.attemptNo) from PaymentAttempt pa where pa.subscription.id = :subscriptionId
			""")
	Optional<Integer> findMaxAttemptNo(UUID subscriptionId);
}