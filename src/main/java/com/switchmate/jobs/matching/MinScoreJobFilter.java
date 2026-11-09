package com.switchmate.jobs.matching;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MinScoreJobFilter implements JobFilter {

	@Override
	public List<MatchedJob> filter(List<MatchedJob> jobs, JobSearchCriteria criteria) {
		if (criteria.minScore() == null) {
			return jobs;
		}
		return jobs.stream().filter(matched -> matched.score() >= criteria.minScore()).toList();
	}
}