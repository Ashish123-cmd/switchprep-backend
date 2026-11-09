package com.switchmate.common.validation;

import com.switchmate.auth.dto.JobPreferencesRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExperienceRangeValidator implements ConstraintValidator<ValidExperienceRange, JobPreferencesRequest> {

	@Override
	public boolean isValid(JobPreferencesRequest value, ConstraintValidatorContext context) {
		if (value == null || value.minExperience() == null || value.maxExperience() == null) {
			return true;
		}
		if (value.minExperience() <= value.maxExperience()) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
				.addPropertyNode("maxExperience")
				.addConstraintViolation();
		return false;
	}
}