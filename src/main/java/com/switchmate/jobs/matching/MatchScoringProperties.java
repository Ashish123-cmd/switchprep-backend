package com.switchmate.jobs.matching;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jobs.matching.weights")
public class MatchScoringProperties {

	private double skills = 0.7;
	private double experience = 0.2;
	private double preferences = 0.1;

	public double getSkills() {
		return skills;
	}

	public void setSkills(double skills) {
		this.skills = skills;
	}

	public double getExperience() {
		return experience;
	}

	public void setExperience(double experience) {
		this.experience = experience;
	}

	public double getPreferences() {
		return preferences;
	}

	public void setPreferences(double preferences) {
		this.preferences = preferences;
	}
}