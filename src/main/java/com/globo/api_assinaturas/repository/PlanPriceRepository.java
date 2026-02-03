package com.globo.api_assinaturas.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.globo.api_assinaturas.domain.PlanPrice;

public interface PlanPriceRepository extends JpaRepository<PlanPrice, UUID> {

	@Query("""
			select pp from PlanPrice pp
			join fetch pp.plan p
			where p.code = :planCode
			  and pp.validFrom <= :at
			  and (pp.validTo is null or pp.validTo > :at)
			order by pp.validFrom desc
			""")
	Optional<PlanPrice> findPriceAt(String planCode, Instant at);

	@Query("""
			select pp from PlanPrice pp
			join fetch pp.plan p
			where p.code = :planCode and pp.validTo is null
			""")
	Optional<PlanPrice> findCurrent(String planCode);

	@Query("""
			   select pp
			   from PlanPrice pp
			   join fetch pp.plan p
			   where p.active = true
			     and pp.validTo is null
			""")
	List<PlanPrice> findCurrentPricesForActivePlans();
}