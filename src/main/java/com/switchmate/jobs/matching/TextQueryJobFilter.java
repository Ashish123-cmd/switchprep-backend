package com.switchmate.jobs.matching;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class TextQueryJobFilter implements JobFilter {

	@Override
	public List<MatchedJob> filter(List<MatchedJob> jobs, JobSearchCriteria criteria) {
		if (criteria.q() == null || criteria.q().isBlank()) {
			return jobs;
		}
		String query = criteria.q().strip().toLowerCase(Locale.ROOT);
		return jobs.stream().filter(matched -> matched.job().title().toLowerCase(Locale.ROOT).contains(query)
				|| matched.job().company().toLowerCase(Locale.ROOT).contains(query)
				|| matched.job().skills().stream().anyMatch(skill -> skill.toLowerCase(Locale.ROOT).contains(query)))
				.toList();
	}
}