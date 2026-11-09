package com.switchmate.jobs.matching;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SavedOnlyJobFilter implements JobFilter {

	@Override
	public List<MatchedJob> filter(List<MatchedJob> jobs, JobSearchCriteria criteria) {
		if (!criteria.savedOnly()) {
			return jobs;
		}
		return jobs.stream().filter(MatchedJob::saved).toList();
	}
}