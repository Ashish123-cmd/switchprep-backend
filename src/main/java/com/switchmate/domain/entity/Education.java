package com.switchmate.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "educations")
public class Education {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "profile_id", nullable = false)
	private UUID profileId;

	@Column(nullable = false)
	private String institution;

	private String degree;
	private String field;

	@Column(name = "start_year")
	private Integer startYear;

	@Column(name = "end_year")
	private Integer endYear;

	protected Education() {
	}

	public Education(UUID profileId, String institution, String degree, String field, Integer startYear,
			Integer endYear) {
		this.profileId = profileId;
		this.institution = institution;
		this.degree = degree;
		this.field = field;
		this.startYear = startYear;
		this.endYear = endYear;
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

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getDegree() {
		return degree;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Integer getStartYear() {
		return startYear;
	}

	public void setStartYear(Integer startYear) {
		this.startYear = startYear;
	}

	public Integer getEndYear() {
		return endYear;
	}

	public void setEndYear(Integer endYear) {
		this.endYear = endYear;
	}
}