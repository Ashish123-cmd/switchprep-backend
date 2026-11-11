package com.switchmate.jobs.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.auth.service.SignupDraftService;

@Component
public class SignupDraftCleanupJob {

	private static final Logger logger = LoggerFactory.getLogger(SignupDraftCleanupJob.class);

	private final SignupDraftService signupDraftService;

	public SignupDraftCleanupJob(SignupDraftService signupDraftService) {
		this.signupDraftService = signupDraftService;
	}

	@Scheduled(cron = "${app.auth.signup-draft.cleanup-cron:0 */5 * * * *}")
	@Transactional
	public void cleanupExpiredDrafts() {
		logger.info("Running scheduled cleanup of expired signup drafts");
		signupDraftService.deleteExpiredDrafts();
		logger.info("Completed cleanup of expired signup drafts");
	}
}