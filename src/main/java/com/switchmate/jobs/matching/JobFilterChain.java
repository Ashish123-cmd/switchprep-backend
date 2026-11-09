package com.switchmate.jobs.matching;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class JobFilterChain {

	private final List<JobFilter> filters;

	public JobFilterChain(List<JobFilter> filters) {
		this.filters = List.copyOf(filters);
	}

	public List<MatchedJob> apply(List<MatchedJob> jobs, JobSearchCriteria criteria) {
		List<MatchedJob> result = jobs;
		for (JobFilter filter : filters) {
			result = filter.filter(result, criteria);
		}
		return result;
	}
}