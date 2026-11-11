package com.switchmate.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.switchmate.auth.dto.GoogleAuthResponse;
import com.switchmate.auth.google.GoogleIdentity;
import com.switchmate.auth.google.GoogleIdentityVerifier;
import com.switchmate.common.exception.InvalidGoogleTokenException;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.User;
import com.switchmate.domain.enums.AuthProvider;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.repository.UserRepository;
import com.switchmate.security.TokenService;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

	private static final String ID_TOKEN = "google-id-token";
	private static final String GOOGLE_SUBJECT = "google-subject-123";
	private static final String EMAIL = "person@example.com";
	private static final String NAME = "Taylor Google";

	@Mock
	private GoogleIdentityVerifier googleIdentityVerifier;
	@Mock
	private UserRepository userRepository;
	@Mock
	private ProfileRepository profileRepository;
	@Mock
	private UserProvisioningService userProvisioningService;
	@Mock
	private TokenService tokenService;

	private GoogleAuthService googleAuthService;

	@BeforeEach
	void setUp() {
		googleAuthService = new GoogleAuthService(
				googleIdentityVerifier,
				userRepository,
				profileRepository,
				userProvisioningService,
				tokenService);
	}

	@Test
	void createsNewGoogleUserWhenNoAccountMatches() {
		UUID userId = UUID.randomUUID();
		User provisionedUser = user(userId, EMAIL, NAME, null);
		when(googleIdentityVerifier.verify(ID_TOKEN)).thenReturn(new GoogleIdentity(
				GOOGLE_SUBJECT, EMAIL, true, NAME));
		when(userRepository.findByGoogleSubject(GOOGLE_SUBJECT)).thenReturn(Optional.empty());
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(userProvisioningService.createGoogleUser(EMAIL, NAME, GOOGLE_SUBJECT))
				.thenReturn(provisionedUser);
		when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId)));
		when(tokenService.issue(userId)).thenReturn("access-token");

		GoogleAuthResponse response = googleAuthService.authenticate(ID_TOKEN);

		assertEquals("access-token", response.token());
		assertEquals(EMAIL, response.user().email());
		assertEquals(NAME, response.user().name());
		assertEquals(userId, response.user().id());
		assertTrue(response.isNewUser());
		verify(userProvisioningService).createGoogleUser(EMAIL, NAME, GOOGLE_SUBJECT);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void reusesExistingGoogleUserFoundBySubject() {
		UUID userId = UUID.randomUUID();
		User existingUser = user(userId, EMAIL, NAME, null);
		existingUser.setAuthProvider(AuthProvider.GOOGLE);
		existingUser.setGoogleSubject(GOOGLE_SUBJECT);
		when(googleIdentityVerifier.verify(ID_TOKEN)).thenReturn(new GoogleIdentity(
				GOOGLE_SUBJECT, EMAIL, true, NAME));
		when(userRepository.findByGoogleSubject(GOOGLE_SUBJECT)).thenReturn(Optional.of(existingUser));
		when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId)));
		when(tokenService.issue(userId)).thenReturn("access-token");

		GoogleAuthResponse response = googleAuthService.authenticate(ID_TOKEN);

		assertEquals("access-token", response.token());
		assertEquals(userId, response.user().id());
		assertFalse(response.isNewUser());
		verify(userRepository, never()).findByEmail(any());
		verify(userRepository, never()).save(any(User.class));
		verify(userProvisioningService, never()).createGoogleUser(any(), any(), any());
	}

	@Test
	void linksExistingLocalAccountByEmailWithoutChangingAuthData() {
		UUID userId = UUID.randomUUID();
		User existingUser = user(userId, EMAIL, "Local Name", "hashed-password");
		when(googleIdentityVerifier.verify(ID_TOKEN)).thenReturn(new GoogleIdentity(
				GOOGLE_SUBJECT, EMAIL, true, NAME));
		when(userRepository.findByGoogleSubject(GOOGLE_SUBJECT)).thenReturn(Optional.empty());
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
		when(userRepository.save(existingUser)).thenReturn(existingUser);
		when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId)));
		when(tokenService.issue(userId)).thenReturn("access-token");

		GoogleAuthResponse response = googleAuthService.authenticate(ID_TOKEN);

		assertEquals("access-token", response.token());
		assertEquals(userId, response.user().id());
		assertFalse(response.isNewUser());
		assertEquals(GOOGLE_SUBJECT, existingUser.getGoogleSubject());
		assertEquals(AuthProvider.LOCAL, existingUser.getAuthProvider());
		assertEquals("hashed-password", existingUser.getPasswordHash());
		verify(userRepository).save(existingUser);
		verify(userProvisioningService, never()).createGoogleUser(any(), any(), any());
	}

	@Test
	void normalizesEmailBeforeLookupAndProvisioning() {
		UUID userId = UUID.randomUUID();
		User provisionedUser = user(userId, EMAIL, NAME, null);
		when(googleIdentityVerifier.verify(ID_TOKEN)).thenReturn(new GoogleIdentity(
				GOOGLE_SUBJECT, " Person@Example.COM ", true, NAME));
		when(userRepository.findByGoogleSubject(GOOGLE_SUBJECT)).thenReturn(Optional.empty());
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
		when(userProvisioningService.createGoogleUser(EMAIL, NAME, GOOGLE_SUBJECT))
				.thenReturn(provisionedUser);
		when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId)));
		when(tokenService.issue(userId)).thenReturn("access-token");

		googleAuthService.authenticate(ID_TOKEN);

		verify(userRepository).findByEmail(EMAIL);
		verify(userProvisioningService).createGoogleUser(EMAIL, NAME, GOOGLE_SUBJECT);
	}

	@Test
	void rejectsUnverifiedGoogleEmail() {
		when(googleIdentityVerifier.verify(ID_TOKEN)).thenReturn(new GoogleIdentity(
				GOOGLE_SUBJECT, EMAIL, false, NAME));

		InvalidGoogleTokenException exception = assertThrows(InvalidGoogleTokenException.class,
				() -> googleAuthService.authenticate(ID_TOKEN));

		assertEquals("Google account email is not verified", exception.getMessage());
		verifyNoInteractions(userRepository, profileRepository, userProvisioningService, tokenService);
	}

	@Test
	void propagatesVerificationFailureWithoutTouchingAccounts() {
		when(googleIdentityVerifier.verify(ID_TOKEN))
				.thenThrow(new InvalidGoogleTokenException("Google ID token is invalid or expired"));

		assertThrows(InvalidGoogleTokenException.class, () -> googleAuthService.authenticate(ID_TOKEN));

		verifyNoInteractions(userRepository, profileRepository, userProvisioningService, tokenService);
	}

	private User user(UUID id, String email, String name, String passwordHash) {
		User user = spy(new User(email, passwordHash, name));
		doReturn(id).when(user).getId();
		return user;
	}
}
