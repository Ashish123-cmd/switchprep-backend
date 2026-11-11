package com.switchmate.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "signup_drafts")
public class SignupDraft {

	@Id
	@Column(name = "id")
	private UUID id;

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "parsed_profile", columnDefinition = "TEXT", nullable = false)
	private String parsedProfile;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	public SignupDraft() {
	}

	public SignupDraft(UUID id, String fileName, String parsedProfile, Instant expiresAt) {
		this.id = id;
		this.fileName = fileName;
		this.parsedProfile = parsedProfile;
		this.expiresAt = expiresAt;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getParsedProfile() {
		return parsedProfile;
	}

	public void setParsedProfile(String parsedProfile) {
		this.parsedProfile = parsedProfile;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}
}