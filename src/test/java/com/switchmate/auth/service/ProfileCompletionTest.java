package com.switchmate.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.switchmate.auth.dto.UserProfileResponse;
import com.switchmate.common.exception.BadRequestException;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.StandardAnswer;
import com.switchmate.domain.entity.User;
import com.switchmate.repository.EducationRepository;
import com.switchmate.repository.ExperienceRepository;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.repository.StandardAnswerRepository;
import com.switchmate.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProfileCompletionTest {

	private final UUID userId = UUID.randomUUID();
	@Mock
	private UserRepository userRepository;
	@Mock
	private ProfileRepository profileRepository;
	@Mock
	private ExperienceRepository experienceRepository;
	@Mock
	private EducationRepository educationRepository;
	@Mock
	private StandardAnswerRepository standardAnswerRepository;

	private ProfileService profileService;
	private User user;
	private Profile profile;
	private StandardAnswer answers;

	@BeforeEach
	void setUp() {
		profileService = new ProfileService(userRepository, profileRepository, experienceRepository,
				educationRepository, standardAnswerRepository,
				new UserAccountLookup(userRepository, profileRepository));
		user = new User("person@example.com", "hash", "Taylor");
		profile = new Profile(userId);
		answers = new StandardAnswer(userId);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
		when(standardAnswerRepository.findById(userId)).thenReturn(Optional.of(answers));
	}

	@Test
	void completionRejectsMissingSkills() {
		assertThrows(BadRequestException.class, () -> profileService.complete(userId));
	}

	@Test
	void completionRejectsMissingNameOrEmail() {
		profile.setSkills(new LinkedHashSet<>(java.util.Set.of("Java")));
		answers.setRequiresSponsorship(false);
		user.setFullName(" ");
		assertThrows(BadRequestException.class, () -> profileService.complete(userId));

		user.setFullName("Taylor");
		user.setEmail(" ");
		assertThrows(BadRequestException.class, () -> profileService.complete(userId));
	}

	@Test
	void completionRequiresSponsorshipAnswer() {
		profile.setSkills(new LinkedHashSet<>(java.util.Set.of("Java")));

		assertThrows(BadRequestException.class, () -> profileService.complete(userId));
	}

	@Test
	void completionMarksProfileCompleteWhenRequirementsArePresent() {
		profile.setSkills(new LinkedHashSet<>(java.util.Set.of("Java")));
		answers.setRequiresSponsorship(false);
		when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(experienceRepository.findAllByProfileId(userId)).thenReturn(java.util.List.of());
		when(educationRepository.findAllByProfileId(userId)).thenReturn(java.util.List.of());

		UserProfileResponse response = profileService.complete(userId);

		assertTrue(profile.isOnboardingCompleted());
		assertTrue(response.onboardingCompleted());
		assertEquals("person@example.com", response.email());
	}
}