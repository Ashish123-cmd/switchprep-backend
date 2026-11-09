package com.switchmate.jobs.matching;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MaxExperienceJobFilter implements JobFilter {

	@Override
	public List<MatchedJob> filter(List<MatchedJob> jobs, JobSearchCriteria criteria) {
		if (criteria.maxExperience() == null) {
			return jobs;
		}
		return jobs.stream().filter(matched -> matched.job().minExperience() == null
				|| matched.job().minExperience() <= criteria.maxExperience()).toList();
	}
}