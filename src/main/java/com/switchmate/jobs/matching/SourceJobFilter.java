package com.switchmate.jobs.matching;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SourceJobFilter implements JobFilter {

	@Override
	public List<MatchedJob> filter(List<MatchedJob> jobs, JobSearchCriteria criteria) {
		if (criteria.sources() == null || criteria.sources().isEmpty()) {
			return jobs;
		}
		return jobs.stream().filter(matched -> criteria.sources().contains(matched.job().source())).toList();
	}
}