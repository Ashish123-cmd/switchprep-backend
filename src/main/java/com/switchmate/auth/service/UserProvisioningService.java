package com.switchmate.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.domain.entity.AutoApplySettings;
import com.switchmate.domain.entity.JobPreference;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.StandardAnswer;
import com.switchmate.domain.entity.User;
import com.switchmate.repository.AutoApplySettingsRepository;
import com.switchmate.repository.JobPreferenceRepository;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.repository.StandardAnswerRepository;
import com.switchmate.repository.UserRepository;

@Service
public class UserProvisioningService {

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final JobPreferenceRepository jobPreferenceRepository;
	private final StandardAnswerRepository standardAnswerRepository;
	private final AutoApplySettingsRepository autoApplySettingsRepository;

	public UserProvisioningService(
			UserRepository userRepository,
			ProfileRepository profileRepository,
			JobPreferenceRepository jobPreferenceRepository,
			StandardAnswerRepository standardAnswerRepository,
			AutoApplySettingsRepository autoApplySettingsRepository) {
		this.userRepository = userRepository;
		this.profileRepository = profileRepository;
		this.jobPreferenceRepository = jobPreferenceRepository;
		this.standardAnswerRepository = standardAnswerRepository;
		this.autoApplySettingsRepository = autoApplySettingsRepository;
	}

	@Transactional
	public User createUserWithDefaults(String email, String passwordHash, String fullName) {
		User user = userRepository.save(new User(email, passwordHash, fullName));
		provisionDefaults(user.getId());
		return user;
	}

	@Transactional
	public void provisionDefaults(UUID userId) {
		profileRepository.save(new Profile(userId));
		jobPreferenceRepository.save(new JobPreference(userId));
		standardAnswerRepository.save(new StandardAnswer(userId));
		autoApplySettingsRepository.save(new AutoApplySettings(userId));
	}
}