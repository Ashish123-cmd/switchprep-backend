package com.switchmate.jobs.ingestion;

public record JobIngestionSummary(
		int sourcesSucceeded,
		int sourcesFailed,
		int jobsCreated,
		int jobsUpdated,
		int jobsDeactivated) {
}