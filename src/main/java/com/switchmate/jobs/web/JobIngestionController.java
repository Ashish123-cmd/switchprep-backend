package com.switchmate.jobs.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.switchmate.common.exception.InvalidAdminApiKeyException;
import com.switchmate.jobs.ingestion.JobIngestionService;
import com.switchmate.jobs.ingestion.JobIngestionService.JobSourceFetch;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/jobs")
@Tag(name = "Job Ingestion", description = "Admin-triggered job ingestion")
public class JobIngestionController {

	private final String configuredAdminApiKey;
	private final JobIngestionService jobIngestionService;

	public JobIngestionController(
			@Value("${app.admin.api-key:}") String configuredAdminApiKey,
			JobIngestionService jobIngestionService) {
		this.configuredAdminApiKey = configuredAdminApiKey;
		this.jobIngestionService = jobIngestionService;
	}

	@PostMapping("/ingest")
	public List<JobSourceFetch> ingest(@RequestHeader(name = "X-Admin-Api-Key", required = false) String apiKey) {
		if (configuredAdminApiKey == null || configuredAdminApiKey.isBlank()
				|| apiKey == null
				|| !MessageDigest.isEqual(
						configuredAdminApiKey.getBytes(StandardCharsets.UTF_8), apiKey.getBytes(StandardCharsets.UTF_8))) {
			throw new InvalidAdminApiKeyException();
		}
		return jobIngestionService.ingestAndFetch();
	}
}