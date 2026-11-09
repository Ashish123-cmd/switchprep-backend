package com.switchmate.autoapply;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.autoapply.dto.AutoApplySettingsRequest;
import com.switchmate.autoapply.dto.AutoApplySettingsResponse;
import com.switchmate.domain.entity.AutoApplySettings;
import com.switchmate.repository.AutoApplySettingsRepository;

@Service
public class AutoApplySettingsService {

	private final AutoApplySettingsRepository settingsRepository;

	public AutoApplySettingsService(AutoApplySettingsRepository settingsRepository) {
		this.settingsRepository = settingsRepository;
	}

	@Transactional
	public AutoApplySettingsResponse get(UUID userId) {
		AutoApplySettings settings = settingsRepository.findById(userId)
				.orElseGet(() -> settingsRepository.save(new AutoApplySettings(userId)));
		return toResponse(settings);
	}

	@Transactional
	public AutoApplySettingsResponse replace(UUID userId, AutoApplySettingsRequest request) {
		AutoApplySettings settings = settingsRepository.findById(userId)
				.orElseGet(() -> new AutoApplySettings(userId));
		settings.setEnabled(request.enabled());
		settings.setMinMatchScore(request.minMatchScore());
		settings.setDailyCap(request.dailyCap());
		settings.setExcludedCompanies(request.excludedCompanies() == null
				? new LinkedHashSet<>() : new LinkedHashSet<>(request.excludedCompanies()));
		settings.setRequireApproval(request.requireApproval() == null || request.requireApproval());
		return toResponse(settingsRepository.save(settings));
	}

	@Transactional(readOnly = true)
	public AutoApplySettingsResponse find(UUID userId) {
		return settingsRepository.findById(userId).map(this::toResponse)
				.orElseGet(() -> new AutoApplySettingsResponse(false, 0, 10, Set.of(), true));
	}

	private AutoApplySettingsResponse toResponse(AutoApplySettings settings) {
		return new AutoApplySettingsResponse(
				settings.isEnabled(),
				settings.getMinMatchScore() == null ? 0 : settings.getMinMatchScore(),
				settings.getDailyCap() == null ? 10 : settings.getDailyCap(),
				Set.copyOf(settings.getExcludedCompanies()),
				settings.isRequireApproval());
	}
}