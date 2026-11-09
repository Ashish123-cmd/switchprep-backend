package com.switchmate.domain.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.switchmate.domain.enums.ApplicationMode;
import com.switchmate.domain.enums.ApplicationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "applications", uniqueConstraints = @UniqueConstraint(name = "uq_applications_user_job", columnNames = { "user_id", "job_id" }))
public class Application {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "job_id", nullable = false)
	private UUID jobId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ApplicationStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "application_mode", nullable = false, length = 50)
	private ApplicationMode mode;

	@Column(name = "updated_at", nullable = false)
	@UpdateTimestamp
	private Instant updatedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Instant createdAt;

	protected Application() {
	}

	public Application(UUID userId, UUID jobId, ApplicationStatus status, ApplicationMode mode) {
		this.userId = userId;
		this.jobId = jobId;
		this.status = status;
		this.mode = mode;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getJobId() {
		return jobId;
	}

	public void setJobId(UUID jobId) {
		this.jobId = jobId;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public void setStatus(ApplicationStatus status) {
		this.status = status;
	}

	public ApplicationMode getMode() {
		return mode;
	}

	public void setMode(ApplicationMode mode) {
		this.mode = mode;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}