package com.switchmate.applications;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.switchmate.common.exception.BadRequestException;
import com.switchmate.domain.enums.ApplicationStatus;

class ApplicationStatusPolicyTest {

	private final ApplicationStatusPolicy policy = new ApplicationStatusPolicy();

	@Test
	void allowsNormalProgression() {
		assertDoesNotThrow(() -> policy.validate(ApplicationStatus.QUEUED, ApplicationStatus.APPLIED));
		assertDoesNotThrow(() -> policy.validate(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW));
		assertDoesNotThrow(() -> policy.validate(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED));
	}

	@Test
	void allowsManualTrackerCorrectionsAndIdempotentUpdates() {
		assertDoesNotThrow(() -> policy.validate(ApplicationStatus.REJECTED, ApplicationStatus.APPLIED));
		assertDoesNotThrow(() -> policy.validate(ApplicationStatus.INTERVIEW, ApplicationStatus.QUEUED));
		assertDoesNotThrow(() -> policy.validate(ApplicationStatus.APPLIED, ApplicationStatus.APPLIED));
	}

	@Test
	void rejectsDisallowedTransition() {
		assertThrows(BadRequestException.class,
				() -> policy.validate(ApplicationStatus.QUEUED, ApplicationStatus.INTERVIEW));
	}
}