package com.switchmate.auth.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.auth.dto.JobPreferencesRequest;
import com.switchmate.auth.dto.JobPreferencesResponse;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.JobPreference;
import com.switchmate.repository.JobPreferenceRepository;

@Service
public class PreferencesService {

	private final JobPreferenceRepository jobPreferenceRepository;

	public PreferencesService(JobPreferenceRepository jobPreferenceRepository) {
		this.jobPreferenceRepository = jobPreferenceRepository;
	}

	@Transactional(readOnly = true)
	public JobPreferencesResponse get(UUID userId) {
		return toResponse(find(userId));
	}

	@Transactional
	public JobPreferencesResponse replace(UUID userId, JobPreferencesRequest request) {
		JobPreference preferences = find(userId);
		preferences.setMinExperience(request.minExperience());
		preferences.setMaxExperience(request.maxExperience());
		preferences.setNoticePeriodDays(request.noticePeriodDays());
		preferences.setExpectedCtcLpa(request.expectedCtcLpa());
		preferences.setWorkAuthorization(request.workAuthorization());
		preferences.setOpenToRelocation(request.openToRelocation());
		preferences.setPreferredRoles(request.preferredRoles() == null
				? new LinkedHashSet<>() : new LinkedHashSet<>(request.preferredRoles()));
		preferences.setPreferredLocations(request.preferredLocations() == null
				? new LinkedHashSet<>() : new LinkedHashSet<>(request.preferredLocations()));
		return toResponse(jobPreferenceRepository.save(preferences));
	}

	private JobPreference find(UUID userId) {
		return jobPreferenceRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Job preferences not found"));
	}

	private JobPreferencesResponse toResponse(JobPreference preferences) {
		return new JobPreferencesResponse(
				preferences.getMinExperience(), preferences.getMaxExperience(), preferences.getNoticePeriodDays(),
				preferences.getExpectedCtcLpa(), preferences.getWorkAuthorization(), preferences.getOpenToRelocation(),
				Set.copyOf(preferences.getPreferredRoles()), Set.copyOf(preferences.getPreferredLocations()));
	}
}