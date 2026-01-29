package com.globo.api_assinaturas.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plan_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PlanPrice {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_code")
	private Plan plan;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(name = "valid_from", nullable = false)
	private Instant validFrom;

	@Column(name = "valid_to")
	private Instant validTo;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	public PlanPrice(UUID id, Plan plan, BigDecimal price, Instant validFrom, Instant validTo) {
		this.id = id;
		this.plan = plan;
		this.price = price;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}
}