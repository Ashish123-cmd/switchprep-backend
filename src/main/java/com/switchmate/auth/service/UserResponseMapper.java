package com.switchmate.auth.service;

import com.switchmate.auth.dto.ProfileResponse;
import com.switchmate.auth.dto.UserResponse;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.User;

final class UserResponseMapper {

	private UserResponseMapper() {
	}

	static UserResponse toResponse(User user, Profile profile) {
		return new UserResponse(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				new ProfileResponse(profile.isOnboardingCompleted()));
	}
}
