package com.switchmate.jobs.ingestion;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.jobs.ingestion.enabled", havingValue = "true", matchIfMissing = true)
public class JobIngestionStartupRunner implements ApplicationRunner {

	private final JobIngestionService jobIngestionService;

	public JobIngestionStartupRunner(JobIngestionService jobIngestionService) {
		this.jobIngestionService = jobIngestionService;
	}

	@Override
	public void run(ApplicationArguments args) {
		jobIngestionService.ingest();
	}
}
