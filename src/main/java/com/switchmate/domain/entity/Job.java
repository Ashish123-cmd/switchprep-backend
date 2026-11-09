package com.switchmate.domain.entity;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.switchmate.domain.enums.JobSource;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "jobs", uniqueConstraints = @UniqueConstraint(name = "uq_jobs_source_external_id", columnNames = { "source", "external_id" }))
public class Job {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private JobSource source;

	@Column(name = "external_id", nullable = false)
	private String externalId;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String company;

	private String location;

	@Column(name = "apply_url", length = 2048)
	private String applyUrl;

	private String description;

	@Column(name = "min_experience")
	private Integer minExperience;

	@Column(name = "max_experience")
	private Integer maxExperience;

	@Column(name = "posted_at")
	private Instant postedAt;

	@Column(nullable = false)
	private boolean active = true;

	@ElementCollection
	@CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
	@Column(name = "skill", nullable = false)
	private Set<String> skills = new LinkedHashSet<>();

	protected Job() {
	}

	public Job(JobSource source, String externalId) {
		this.source = source;
		this.externalId = externalId;
	}

	public UUID getId() {
		return id;
	}

	public JobSource getSource() {
		return source;
	}

	public void setSource(JobSource source) {
		this.source = source;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getApplyUrl() {
		return applyUrl;
	}

	public void setApplyUrl(String applyUrl) {
		this.applyUrl = applyUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public Instant getPostedAt() {
		return postedAt;
	}

	public void setPostedAt(Instant postedAt) {
		this.postedAt = postedAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Set<String> getSkills() {
		return skills;
	}

	public void setSkills(Set<String> skills) {
		this.skills = skills;
	}
}