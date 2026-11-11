package com.switchmate.auth.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.User;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.repository.UserRepository;

/**
 * Single source of truth for loading the account rows that every auth and
 * profile use case needs, so the same {@link NotFoundException} contract is
 * applied everywhere.
 */
@Component
public class UserAccountLookup {

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;

	public UserAccountLookup(UserRepository userRepository, ProfileRepository profileRepository) {
		this.userRepository = userRepository;
		this.profileRepository = profileRepository;
	}

	public User requireUser(UUID userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User not found"));
	}

	public Profile requireProfile(UUID userId) {
		return profileRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Profile not found"));
	}
}
