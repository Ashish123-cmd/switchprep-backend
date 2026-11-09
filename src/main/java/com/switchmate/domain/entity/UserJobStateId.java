package com.switchmate.domain.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserJobStateId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "job_id")
	private UUID jobId;

	protected UserJobStateId() {
	}

	public UserJobStateId(UUID userId, UUID jobId) {
		this.userId = userId;
		this.jobId = jobId;
	}

	public UUID getUserId() {
		return userId;
	}

	public UUID getJobId() {
		return jobId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UserJobStateId that)) {
			return false;
		}
		return Objects.equals(userId, that.userId) && Objects.equals(jobId, that.jobId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, jobId);
	}
}