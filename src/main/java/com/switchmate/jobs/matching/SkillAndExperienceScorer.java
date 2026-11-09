package com.switchmate.jobs.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.switchmate.domain.entity.Job;

@Component
public class SkillAndExperienceScorer implements MatchScorer {

	private final MatchScoringProperties weights;

	public SkillAndExperienceScorer(MatchScoringProperties weights) {
		this.weights = weights;
	}

	@Override
	public MatchResult score(UserProfile profile, JobPreferences prefs, Job job) {
		double skillFit = skillFit(profile.skills(), job.getSkills());
		double experienceFit = experienceFit(profile.totalExperienceYears(), job.getMinExperience(), job.getMaxExperience());
		double preferenceFit = preferenceFit(prefs, job);
		double totalWeight = weights.getSkills() + weights.getExperience() + weights.getPreferences();
		int score = totalWeight <= 0 ? 0 : (int) Math.round(100 * (
				(skillFit * weights.getSkills() + experienceFit * weights.getExperience()
						+ preferenceFit * weights.getPreferences()) / totalWeight));

		List<String> reasons = new ArrayList<>(3);
		Set<String> jobSkills = normalized(job.getSkills());
		Set<String> profileSkills = normalized(profile.skills());
		long overlappingSkills = jobSkills.stream().filter(profileSkills::contains).count();
		reasons.add(jobSkills.isEmpty()
				? "The job description has no listed skills to compare"
				: "Matches " + overlappingSkills + " of " + jobSkills.size() + " listed skills");
		reasons.add(experienceReason(profile.totalExperienceYears(), job.getMinExperience(), job.getMaxExperience()));
		reasons.add(preferenceReason(prefs, job));
		return new MatchResult(Math.max(0, Math.min(100, score)), List.copyOf(reasons));
	}

	private double skillFit(Set<String> profileSkills, Set<String> jobSkills) {
		Set<String> normalizedJobSkills = normalized(jobSkills);
		if (normalizedJobSkills.isEmpty()) {
			return 1;
		}
		Set<String> normalizedProfileSkills = normalized(profileSkills);
		return (double) normalizedJobSkills.stream().filter(normalizedProfileSkills::contains).count()
				/ normalizedJobSkills.size();
	}

	private double experienceFit(Integer years, Integer minimum, Integer maximum) {
		if (minimum == null && maximum == null) {
			return 1;
		}
		if (years == null) {
			return 0.5;
		}
		if (minimum != null && years < minimum) {
			return Math.max(0, 1.0 - (double) (minimum - years) / Math.max(minimum, 1));
		}
		if (maximum != null && years > maximum) {
			return Math.max(0, 1.0 - (double) (years - maximum) / Math.max(maximum, 1));
		}
		return 1;
	}

	private double preferenceFit(JobPreferences prefs, Job job) {
		int dimensions = 0;
		int matches = 0;
		if (prefs.preferredRoles() != null && !prefs.preferredRoles().isEmpty()) {
			dimensions++;
			if (containsAny(job.getTitle(), prefs.preferredRoles())) {
				matches++;
			}
		}
		if (prefs.preferredLocations() != null && !prefs.preferredLocations().isEmpty()) {
			dimensions++;
			if (containsAny(job.getLocation(), prefs.preferredLocations())) {
				matches++;
			}
		}
		return dimensions == 0 ? 1 : (double) matches / dimensions;
	}

	private String experienceReason(Integer years, Integer minimum, Integer maximum) {
		if (minimum == null && maximum == null) {
			return "No experience range is specified for this job";
		}
		if (years == null) {
			return "Add your experience level to compare it with this job";
		}
		if ((minimum == null || years >= minimum) && (maximum == null || years <= maximum)) {
			return "Your experience fits the requested range";
		}
		return "Your experience is outside the requested range";
	}

	private String preferenceReason(JobPreferences prefs, Job job) {
		boolean roleMatched = prefs.preferredRoles() == null || prefs.preferredRoles().isEmpty()
				|| containsAny(job.getTitle(), prefs.preferredRoles());
		boolean locationMatched = prefs.preferredLocations() == null || prefs.preferredLocations().isEmpty()
				|| containsAny(job.getLocation(), prefs.preferredLocations());
		if (roleMatched && locationMatched) {
			return "Matches your role and location preferences";
		}
		if (!roleMatched && !locationMatched) {
			return "Does not match your preferred roles or locations";
		}
		return roleMatched ? "Matches your preferred role" : "Matches your preferred location";
	}

	private boolean containsAny(String value, Set<String> candidates) {
		if (value == null) {
			return false;
		}
		String normalizedValue = value.toLowerCase(Locale.ROOT);
		return candidates.stream().filter(candidate -> candidate != null && !candidate.isBlank())
				.anyMatch(candidate -> normalizedValue.contains(candidate.strip().toLowerCase(Locale.ROOT)));
	}

	private Set<String> normalized(Set<String> values) {
		if (values == null) {
			return Set.of();
		}
		return values.stream().filter(value -> value != null && !value.isBlank())
				.map(value -> value.strip().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
	}
}