package com.switchmate.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.switchmate.auth.dto.AuthResponse;
import com.switchmate.auth.dto.LoginRequest;
import com.switchmate.auth.dto.SignupRequest;
import com.switchmate.common.exception.ConflictException;
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
import com.switchmate.security.TokenService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private ProfileRepository profileRepository;
	@Mock
	private JobPreferenceRepository jobPreferenceRepository;
	@Mock
	private StandardAnswerRepository standardAnswerRepository;
	@Mock
	private AutoApplySettingsRepository autoApplySettingsRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private TokenService tokenService;

	@InjectMocks
	private AuthService authService;

	@Test
	void signupCreatesDefaultRowsAndReturnsToken() {
		UUID userId = UUID.randomUUID();
		User savedUser = user(userId, "person@example.com", "Taylor", "hashed-password");
		when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
		when(passwordEncoder.encode("SecurePass123")).thenReturn("hashed-password");
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(tokenService.issue(userId)).thenReturn("access-token");

		AuthResponse response = authService.signup(
				new SignupRequest("Taylor", "PERSON@EXAMPLE.COM", "SecurePass123"));

		assertEquals("access-token", response.token());
		assertEquals(userId, response.user().id());
		assertFalse(response.user().profile().onboardingCompleted());
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		assertEquals("person@example.com", userCaptor.getValue().getEmail());
		assertEquals("hashed-password", userCaptor.getValue().getPasswordHash());
		verify(jobPreferenceRepository).save(any(JobPreference.class));
		verify(standardAnswerRepository).save(any(StandardAnswer.class));
		verify(autoApplySettingsRepository).save(any(AutoApplySettings.class));
	}

	@Test
	void signupRejectsDuplicateEmail() {
		when(userRepository.existsByEmail("person@example.com")).thenReturn(true);

		assertThrows(ConflictException.class,
				() -> authService.signup(new SignupRequest("Taylor", "person@example.com", "SecurePass123")));
	}

	@Test
	void loginRejectsWrongPasswordWithGenericCredentialError() {
		User savedUser = user(null, "person@example.com", "Taylor", "hashed-password");
		when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(savedUser));
		when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

		assertThrows(BadCredentialsException.class,
				() -> authService.login(new LoginRequest("person@example.com", "wrong-password")));
	}

	@Test
	void loginReturnsTokenForValidCredentials() {
		UUID userId = UUID.randomUUID();
		User savedUser = user(userId, "person@example.com", "Taylor", "hashed-password");
		when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(savedUser));
		when(passwordEncoder.matches("SecurePass123", "hashed-password")).thenReturn(true);
		when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId)));
		when(tokenService.issue(userId)).thenReturn("access-token");

		AuthResponse response = authService.login(new LoginRequest("person@example.com", "SecurePass123"));

		assertEquals("access-token", response.token());
		assertEquals("person@example.com", response.user().email());
	}

	private User user(UUID id, String email, String name, String passwordHash) {
		User user = spy(new User(email, passwordHash, name));
		if (id != null) {
			doReturn(id).when(user).getId();
		}
		return user;
	}
}