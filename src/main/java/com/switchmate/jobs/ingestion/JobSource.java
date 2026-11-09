package com.switchmate.jobs.ingestion;

import java.util.List;

import com.switchmate.domain.enums.JobSourceType;

public interface JobSource {

	JobSourceType type();

	List<RawJob> fetchJobs();
}