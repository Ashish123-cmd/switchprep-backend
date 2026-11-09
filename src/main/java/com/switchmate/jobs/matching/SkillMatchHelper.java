package com.switchmate.jobs.matching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class SkillMatchHelper {

	public SkillBreakdown compare(Set<String> profileSkills, Set<String> jobSkills) {
		Set<String> normalizedProfileSkills = new HashSet<>();
		if (profileSkills != null) {
			profileSkills.stream().filter(this::hasText)
					.map(skill -> normalize(skill)).forEach(normalizedProfileSkills::add);
		}

		List<String> matched = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		if (jobSkills != null) {
			Set<String> seen = new HashSet<>();
			for (String skill : jobSkills) {
				if (!hasText(skill)) {
					continue;
				}
				String key = normalize(skill);
				if (seen.add(key)) {
					(ifMatched(normalizedProfileSkills, key) ? matched : missing).add(skill.strip());
				}
			}
		}
		return new SkillBreakdown(List.copyOf(matched), List.copyOf(missing));
	}

	private boolean ifMatched(Set<String> profileSkills, String jobSkill) {
		return profileSkills.contains(jobSkill);
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private String normalize(String value) {
		return value.strip().toLowerCase(Locale.ROOT);
	}
}