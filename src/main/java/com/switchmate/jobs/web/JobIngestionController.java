package com.switchmate.jobs.web;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.switchmate.jobs.ingestion.JobIngestionService;
import com.switchmate.jobs.ingestion.JobIngestionService.JobSourceFetch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/jobs")
@Tag(name = "Job Ingestion", description = "Admin-triggered job ingestion")
@SecurityRequirement(name = "adminApiKey")
public class JobIngestionController {

	private final JobIngestionService jobIngestionService;

	public JobIngestionController(JobIngestionService jobIngestionService) {
		this.jobIngestionService = jobIngestionService;
	}

	@PostMapping("/ingest")
	@Operation(
			summary = "Trigger job ingestion",
			description = "Fetches jobs from every configured board. Requires the X-Admin-Api-Key header; "
					+ "this endpoint is authenticated with the admin API key instead of a user JWT.")
	public List<JobSourceFetch> ingest() {
		return jobIngestionService.ingestAndFetch();
	}
}