package com.switchmate.domain.entity;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "auto_apply_settings")
public class AutoApplySettings {

	@Id
	@Column(name = "user_id")
	private UUID userId;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "min_match_score")
	private Integer minMatchScore = 0;

	@Column(name = "daily_cap")
	private Integer dailyCap = 10;

	@Column(name = "require_approval", nullable = false)
	private boolean requireApproval = true;

	@ElementCollection
	@CollectionTable(name = "excluded_companies", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "company", nullable = false)
	private Set<String> excludedCompanies = new LinkedHashSet<>();

	protected AutoApplySettings() {
	}

	public AutoApplySettings(UUID userId) {
		this.userId = userId;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Integer getMinMatchScore() {
		return minMatchScore;
	}

	public void setMinMatchScore(Integer minMatchScore) {
		this.minMatchScore = minMatchScore;
	}

	public Integer getDailyCap() {
		return dailyCap;
	}

	public void setDailyCap(Integer dailyCap) {
		this.dailyCap = dailyCap;
	}

	public boolean isRequireApproval() {
		return requireApproval;
	}

	public void setRequireApproval(boolean requireApproval) {
		this.requireApproval = requireApproval;
	}

	public Set<String> getExcludedCompanies() {
		return excludedCompanies;
	}

	public void setExcludedCompanies(Set<String> excludedCompanies) {
		this.excludedCompanies = excludedCompanies;
	}
}