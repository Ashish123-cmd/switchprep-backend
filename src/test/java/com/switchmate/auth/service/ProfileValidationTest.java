package com.switchmate.auth.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.switchmate.auth.dto.JobPreferencesRequest;
import com.switchmate.auth.dto.StandardAnswersRequest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ProfileValidationTest {

	private static Validator validator;

	@BeforeAll
	static void createValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void rejectsMinimumExperienceGreaterThanMaximum() {
		JobPreferencesRequest request = new JobPreferencesRequest(
				5, 2, 0, BigDecimal.ZERO, null, null, Set.of(), Set.of());

		assertFalse(validator.validate(request).isEmpty());
	}

	@Test
	void rejectsNegativeExperience() {
		JobPreferencesRequest request = new JobPreferencesRequest(
				-1, 2, 0, BigDecimal.ZERO, null, null, Set.of(), Set.of());

		assertFalse(validator.validate(request).isEmpty());
	}

	@Test
	void rejectsMalformedUrls() {
		StandardAnswersRequest request = new StandardAnswersRequest(
				null, null, "not-a-url", null, null, java.util.Map.of());

		assertFalse(validator.validate(request).isEmpty());
	}

	@Test
	void acceptsNullBoundsAndNonNegativeValues() {
		JobPreferencesRequest request = new JobPreferencesRequest(
				null, 2, 0, BigDecimal.ZERO, null, null, Set.of(), Set.of());

		assertTrue(validator.validate(request).isEmpty());
	}
}