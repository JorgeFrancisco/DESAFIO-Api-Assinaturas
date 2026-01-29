package com.globo.api_assinaturas.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.globo.api_assinaturas.domain.enums.PaymentAttemptStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "payment_attempt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PaymentAttempt {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subscription_id")
	private Subscription subscription;

	@Column(name = "attempt_no", nullable = false)
	private int attemptNo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentAttemptStatus status;

	@Column(name = "error_message", length = 500)
	private String errorMessage;

	@Column(name = "charged_amount", precision = 10, scale = 2)
	private BigDecimal chargedAmount;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	public PaymentAttempt(UUID id, Subscription subscription, int attemptNo, PaymentAttemptStatus status,
			String errorMessage, BigDecimal chargedAmount) {
		this.id = id;
		this.subscription = subscription;
		this.attemptNo = attemptNo;
		this.status = status;
		this.errorMessage = errorMessage;
		this.chargedAmount = chargedAmount;
	}
}