package com.switchmate.domain.entity;

import java.time.Instant;
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
@Table(name = "profiles")
public class Profile {

	@Id
	@Column(name = "user_id")
	private UUID userId;

	private String phone;
	private String location;
	private String headline;

	@Column(length = 4000)
	private String summary;

	@Column(name = "total_experience_years")
	private Integer totalExperienceYears;

	@Column(name = "resume_file_name")
	private String resumeFileName;

	@Column(name = "resume_uploaded_at")
	private Instant resumeUploadedAt;

	@Column(name = "onboarding_completed", nullable = false)
	private boolean onboardingCompleted;

	@ElementCollection
	@CollectionTable(name = "profile_skills", joinColumns = @JoinColumn(name = "profile_id"))
	@Column(name = "skill", nullable = false)
	private Set<String> skills = new LinkedHashSet<>();

	protected Profile() {
	}

	public Profile(UUID userId) {
		this.userId = userId;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Integer getTotalExperienceYears() {
		return totalExperienceYears;
	}

	public void setTotalExperienceYears(Integer totalExperienceYears) {
		this.totalExperienceYears = totalExperienceYears;
	}

	public String getResumeFileName() {
		return resumeFileName;
	}

	public void setResumeFileName(String resumeFileName) {
		this.resumeFileName = resumeFileName;
	}

	public Instant getResumeUploadedAt() {
		return resumeUploadedAt;
	}

	public void setResumeUploadedAt(Instant resumeUploadedAt) {
		this.resumeUploadedAt = resumeUploadedAt;
	}

	public boolean isOnboardingCompleted() {
		return onboardingCompleted;
	}

	public void setOnboardingCompleted(boolean onboardingCompleted) {
		this.onboardingCompleted = onboardingCompleted;
	}

	public Set<String> getSkills() {
		return skills;
	}

	public void setSkills(Set<String> skills) {
		this.skills = skills;
	}
}