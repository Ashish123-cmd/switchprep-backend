package com.switchmate.domain.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.switchmate.domain.enums.AuthProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "password_hash")
	private String passwordHash;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Column(name = "auth_provider", nullable = false, length = 20)
	private AuthProvider authProvider = AuthProvider.LOCAL;

	@Column(name = "google_subject")
	private String googleSubject;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Instant createdAt;

	protected User() {
	}

	public User(String email, String passwordHash, String fullName) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.authProvider = AuthProvider.LOCAL;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public AuthProvider getAuthProvider() {
		return authProvider;
	}

	public void setAuthProvider(AuthProvider authProvider) {
		this.authProvider = authProvider;
	}

	public String getGoogleSubject() {
		return googleSubject;
	}

	public void setGoogleSubject(String googleSubject) {
		this.googleSubject = googleSubject;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}