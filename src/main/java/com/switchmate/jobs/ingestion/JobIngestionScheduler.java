package com.switchmate.jobs.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.jobs.ingestion.enabled", havingValue = "true", matchIfMissing = true)
public class JobIngestionScheduler {

	private static final Logger logger = LoggerFactory.getLogger(JobIngestionScheduler.class);

	private final JobIngestionService jobIngestionService;

	public JobIngestionScheduler(JobIngestionService jobIngestionService) {
		this.jobIngestionService = jobIngestionService;
	}

	@Scheduled(cron = "${app.jobs.ingestion.cron:0 0 */6 * * *}")
	public void ingestOnSchedule() {
		logger.info("Scheduled job ingestion completed: {}", jobIngestionService.ingest());
	}
}