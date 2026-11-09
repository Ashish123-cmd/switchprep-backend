package com.switchmate.domain.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_job_state")
public class UserJobState {

	@EmbeddedId
	private UserJobStateId id;

	private boolean saved;
	private boolean hidden;

	protected UserJobState() {
	}

	public UserJobState(UserJobStateId id) {
		this.id = id;
	}

	public UserJobStateId getId() {
		return id;
	}

	public void setId(UserJobStateId id) {
		this.id = id;
	}

	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
}