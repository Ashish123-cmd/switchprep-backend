package com.switchmate.jobs.matching;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class LocationJobFilter implements JobFilter {

	@Override
	public List<MatchedJob> filter(List<MatchedJob> jobs, JobSearchCriteria criteria) {
		if (criteria.location() == null || criteria.location().isBlank()) {
			return jobs;
		}
		String location = criteria.location().strip().toLowerCase(Locale.ROOT);
		return jobs.stream().filter(matched -> matched.job().location() != null
				&& matched.job().location().toLowerCase(Locale.ROOT).contains(location)).toList();
	}
}