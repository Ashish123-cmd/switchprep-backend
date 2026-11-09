package com.switchmate.jobs.matching;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MinExperienceJobFilter implements JobFilter {

	@Override
	public List<MatchedJob> filter(List<MatchedJob> jobs, JobSearchCriteria criteria) {
		if (criteria.minExperience() == null) {
			return jobs;
		}
		return jobs.stream().filter(matched -> matched.job().maxExperience() == null
				|| matched.job().maxExperience() >= criteria.minExperience()).toList();
	}
}