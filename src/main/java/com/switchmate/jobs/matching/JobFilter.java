package com.switchmate.jobs.matching;

import java.util.List;

public interface JobFilter {

	List<MatchedJob> filter(List<MatchedJob> jobs, JobSearchCriteria criteria);
}