package com.globo.api_assinaturas.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.globo.api_assinaturas.enums.SubscriptionStatus;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Subscription {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_code")
	private Plan plan;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SubscriptionStatus status;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "expiration_date", nullable = false)
	private LocalDate expirationDate;

	@Column(name = "failed_attempts", nullable = false)
	private int failedAttempts;

	@Column(name = "next_retry_at")
	private Instant nextRetryAt;

	@Column(name = "last_attempt_at")
	private Instant lastAttemptAt;

	@Column(name = "last_charged_amount", precision = 10, scale = 2)
	private BigDecimal lastChargedAmount;

	@Version
	private long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	public Subscription(UUID id, AppUser user, Plan plan, SubscriptionStatus status, LocalDate startDate,
			LocalDate expirationDate) {
		this.id = id;
		this.user = user;
		this.plan = plan;
		this.status = status;
		this.startDate = startDate;
		this.expirationDate = expirationDate;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = Instant.now();
	}

	public boolean hasAccess(LocalDate today) {
		return !today.isAfter(expirationDate);
	}

	public void cancel() {
		// Mantem acesso ate expirationDate
		this.status = SubscriptionStatus.CANCELED;
	}

	public void markAttemptedNow(Instant now) {
		this.lastAttemptAt = now;
	}

	public void markPaidAndExtend(LocalDate newExpiration, BigDecimal charged, Instant now) {
		this.failedAttempts = 0;
		this.nextRetryAt = null;
		this.lastChargedAmount = charged;
		this.lastAttemptAt = now;
		this.expirationDate = newExpiration;
		this.status = SubscriptionStatus.ACTIVE;
	}

	public void markFailedAttempt(int newFailedAttempts, Instant nextRetryAt, Instant now) {
		this.failedAttempts = newFailedAttempts;
		this.nextRetryAt = nextRetryAt;
		this.lastAttemptAt = now;
	}

	public void suspend() {
		this.status = SubscriptionStatus.SUSPENDED;
		this.nextRetryAt = null;
	}

	public void forceExpirationDate(LocalDate date) {
		this.expirationDate = date;
	}
}