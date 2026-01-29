package com.globo.api_assinaturas.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.globo.api_assinaturas.domain.Subscription;
import com.globo.api_assinaturas.domain.enums.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

	@Query("""
			select s from Subscription s
			join fetch s.user u
			join fetch s.plan p
			where u.id = :userId and s.status = :status
			""")
	Optional<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

	// Scheduler: buscar assinaturas devidas com lock (native)
	@Query(value = """
			select s.*
			from subscription s
			where s.status = 'ACTIVE'
			  and (s.expiration_date <= current_date)
			  and (s.next_retry_at is null or s.next_retry_at <= :now)
			order by s.expiration_date asc
			for update skip locked
			limit :batch
			""", nativeQuery = true)
	List<Subscription> lockDueSubscriptions(Instant now, int batch);

	@Query("""
			  select s from Subscription s
			  join fetch s.user u
			  join fetch s.plan p
			  where u.id = :userId
			  order by s.createdAt desc
			""")
	List<Subscription> findLatestByUserId(UUID userId, org.springframework.data.domain.Pageable pageable);
}