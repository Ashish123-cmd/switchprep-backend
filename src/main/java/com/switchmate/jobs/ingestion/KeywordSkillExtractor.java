package com.switchmate.jobs.ingestion;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeywordSkillExtractor implements SkillExtractor {

	private final List<SkillKeyword> keywords;

	public KeywordSkillExtractor(@Value("${app.jobs.known-skills:}") List<String> knownSkills) {
		this.keywords = knownSkills.stream()
				.filter(skill -> skill != null && !skill.isBlank())
				.map(skill -> new SkillKeyword(skill.strip(), Pattern.compile(
						"(?i)(?<![\\p{L}\\p{N}])" + Pattern.quote(skill.strip()) + "(?![\\p{L}\\p{N}])")))
				.toList();
	}

	@Override
	public Set<String> extract(String title, String description) {
		String searchableText = (title == null ? "" : title) + " " + (description == null ? "" : description);
		Set<String> matches = new LinkedHashSet<>();
		for (SkillKeyword keyword : keywords) {
			if (keyword.pattern().matcher(searchableText).find()) {
				matches.add(keyword.name());
			}
		}
		return matches;
	}

	private record SkillKeyword(String name, Pattern pattern) {
	}
}