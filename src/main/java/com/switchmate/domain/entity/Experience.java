package com.switchmate.domain.entity;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "experiences")
public class Experience {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "profile_id", nullable = false)
	private UUID profileId;

	@Column(nullable = false)
	private String company;

	@Column(nullable = false)
	private String title;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(length = 4000)
	private String description;

	protected Experience() {
	}

	public Experience(UUID profileId, String company, String title, LocalDate startDate, LocalDate endDate,
			String description) {
		this.profileId = profileId;
		this.company = company;
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
		this.description = description;
	}

	public UUID getId() {
		return id;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public void setProfileId(UUID profileId) {
		this.profileId = profileId;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}