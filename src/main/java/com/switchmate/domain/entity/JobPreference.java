package com.switchmate.domain.entity;

import java.math.BigDecimal;
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
@Table(name = "job_preferences")
public class JobPreference {

	@Id
	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "min_experience")
	private Integer minExperience;

	@Column(name = "max_experience")
	private Integer maxExperience;

	@Column(name = "notice_period_days")
	private Integer noticePeriodDays;

	@Column(name = "expected_ctc_lpa", precision = 10, scale = 2)
	private BigDecimal expectedCtcLpa;

	@Column(name = "work_authorization")
	private String workAuthorization;

	@Column(name = "open_to_relocation")
	private Boolean openToRelocation;

	@ElementCollection
	@CollectionTable(name = "preferred_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role", nullable = false)
	private Set<String> preferredRoles = new LinkedHashSet<>();

	@ElementCollection
	@CollectionTable(name = "preferred_locations", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "location", nullable = false)
	private Set<String> preferredLocations = new LinkedHashSet<>();

	protected JobPreference() {
	}

	public JobPreference(UUID userId) {
		this.userId = userId;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public Integer getMinExperience() {
		return minExperience;
	}

	public void setMinExperience(Integer minExperience) {
		this.minExperience = minExperience;
	}

	public Integer getMaxExperience() {
		return maxExperience;
	}

	public void setMaxExperience(Integer maxExperience) {
		this.maxExperience = maxExperience;
	}

	public Integer getNoticePeriodDays() {
		return noticePeriodDays;
	}

	public void setNoticePeriodDays(Integer noticePeriodDays) {
		this.noticePeriodDays = noticePeriodDays;
	}

	public BigDecimal getExpectedCtcLpa() {
		return expectedCtcLpa;
	}

	public void setExpectedCtcLpa(BigDecimal expectedCtcLpa) {
		this.expectedCtcLpa = expectedCtcLpa;
	}

	public String getWorkAuthorization() {
		return workAuthorization;
	}

	public void setWorkAuthorization(String workAuthorization) {
		this.workAuthorization = workAuthorization;
	}

	public Boolean getOpenToRelocation() {
		return openToRelocation;
	}

	public void setOpenToRelocation(Boolean openToRelocation) {
		this.openToRelocation = openToRelocation;
	}

	public Set<String> getPreferredRoles() {
		return preferredRoles;
	}

	public void setPreferredRoles(Set<String> preferredRoles) {
		this.preferredRoles = preferredRoles;
	}

	public Set<String> getPreferredLocations() {
		return preferredLocations;
	}

	public void setPreferredLocations(Set<String> preferredLocations) {
		this.preferredLocations = preferredLocations;
	}
}