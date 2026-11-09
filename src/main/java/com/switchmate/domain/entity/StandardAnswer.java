package com.switchmate.domain.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "standard_answers")
public class StandardAnswer {

	@Id
	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "requires_sponsorship")
	private Boolean requiresSponsorship;

	@Column(name = "willing_to_relocate")
	private Boolean willingToRelocate;

	@Column(name = "linkedin_url", length = 2048)
	private String linkedinUrl;

	@Column(name = "github_url", length = 2048)
	private String githubUrl;

	@Column(name = "portfolio_url", length = 2048)
	private String portfolioUrl;

	@ElementCollection
	@CollectionTable(name = "custom_answers", joinColumns = @JoinColumn(name = "user_id"))
	@MapKeyColumn(name = "question", length = 1000)
	@Column(name = "answer", nullable = false)
	private Map<String, String> customAnswers = new LinkedHashMap<>();

	protected StandardAnswer() {
	}

	public StandardAnswer(UUID userId) {
		this.userId = userId;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public Boolean getRequiresSponsorship() {
		return requiresSponsorship;
	}

	public void setRequiresSponsorship(Boolean requiresSponsorship) {
		this.requiresSponsorship = requiresSponsorship;
	}

	public Boolean getWillingToRelocate() {
		return willingToRelocate;
	}

	public void setWillingToRelocate(Boolean willingToRelocate) {
		this.willingToRelocate = willingToRelocate;
	}

	public String getLinkedinUrl() {
		return linkedinUrl;
	}

	public void setLinkedinUrl(String linkedinUrl) {
		this.linkedinUrl = linkedinUrl;
	}

	public String getGithubUrl() {
		return githubUrl;
	}

	public void setGithubUrl(String githubUrl) {
		this.githubUrl = githubUrl;
	}

	public String getPortfolioUrl() {
		return portfolioUrl;
	}

	public void setPortfolioUrl(String portfolioUrl) {
		this.portfolioUrl = portfolioUrl;
	}

	public Map<String, String> getCustomAnswers() {
		return customAnswers;
	}

	public void setCustomAnswers(Map<String, String> customAnswers) {
		this.customAnswers = customAnswers;
	}
}