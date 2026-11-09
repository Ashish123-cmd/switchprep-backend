package com.switchmate.jobs.ingestion;

import java.util.List;

public class IncompleteJobFetchException extends RuntimeException {

	private final List<RawJob> fetchedJobs;

	public IncompleteJobFetchException(String message, List<RawJob> fetchedJobs) {
		super(message);
		this.fetchedJobs = List.copyOf(fetchedJobs);
	}

	public List<RawJob> fetchedJobs() {
		return fetchedJobs;
	}
}