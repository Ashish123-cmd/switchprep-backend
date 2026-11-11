package com.switchmate.auth.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.switchmate.auth.dto.AuthResponse;
import com.switchmate.auth.dto.GoogleAuthRequest;
import com.switchmate.auth.dto.GoogleAuthResponse;
import com.switchmate.auth.dto.LoginRequest;
import com.switchmate.auth.dto.SignupCompleteRequest;
import com.switchmate.auth.dto.UserResponse;
import com.switchmate.auth.service.AuthService;
import com.switchmate.auth.service.GoogleAuthService;
import com.switchmate.auth.service.SignupDraftService;
import com.switchmate.common.exception.RateLimitExceededException;
import com.switchmate.common.ratelimit.RateLimiter;
import com.switchmate.security.CurrentUser;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Resume-driven signup, login, Google sign-in, and current-user endpoints")
public class AuthController {

	private final AuthService authService;
	private final GoogleAuthService googleAuthService;
	private final SignupDraftService signupDraftService;
	private final RateLimiter rateLimiter;

	public AuthController(
			AuthService authService,
			GoogleAuthService googleAuthService,
			SignupDraftService signupDraftService,
			RateLimiter rateLimiter) {
		this.authService = authService;
		this.googleAuthService = googleAuthService;
		this.signupDraftService = signupDraftService;
		this.rateLimiter = rateLimiter;
	}

	@PostMapping(value = "/signup/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityRequirements
	@Operation(
			summary = "Parse a resume for signup",
			description = "Public. Stores the uploaded PDF/DOCX as a short-lived signup draft, parses it, and "
					+ "returns the draft id plus the parsed profile. The draft id must be sent to "
					+ "/api/auth/signup/complete. Uploads are rate limited per client IP.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Draft created"),
			@ApiResponse(responseCode = "400", description = "Missing, oversized, or unsupported resume file"),
			@ApiResponse(responseCode = "422", description = "The resume could not be parsed"),
			@ApiResponse(responseCode = "429", description = "Too many upload attempts")
	})
	public SignupDraftService.SignupDraftResponse uploadSignupResume(
			@RequestPart("file") MultipartFile file,
			HttpServletRequest request) {
		String clientIp = request.getRemoteAddr();
		if (!rateLimiter.tryAcquire(clientIp)) {
			throw new RateLimitExceededException("Too many resume upload attempts. Please try again later.");
		}
		return signupDraftService.createDraft(file);
	}

	@PostMapping("/signup/complete")
	@SecurityRequirements
	@Operation(
			summary = "Complete signup",
			description = "Public. Creates the account from a signup draft, applies the parsed profile, moves the "
					+ "resume out of the draft area, and returns a JWT plus the new user.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Account created"),
			@ApiResponse(responseCode = "400", description = "Invalid request body"),
			@ApiResponse(responseCode = "409", description = "Email is already registered"),
			@ApiResponse(responseCode = "410", description = "Signup draft is missing or expired")
	})
	public AuthResponse completeSignup(@Valid @RequestBody SignupCompleteRequest request) {
		return authService.completeSignup(request);
	}

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Log in", description = "Public. Exchanges email and password for a JWT and the user.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Authenticated"),
			@ApiResponse(responseCode = "400", description = "Invalid request body"),
			@ApiResponse(responseCode = "401", description = "Invalid email or password")
	})
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/google")
	@SecurityRequirements
	@Operation(
			summary = "Sign in with Google",
			description = "Public. Verifies a Google ID token, links the account by Google subject or email, "
					+ "provisions a new account when needed, and returns a JWT plus the user.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Authenticated"),
			@ApiResponse(responseCode = "400", description = "Invalid request body"),
			@ApiResponse(responseCode = "401", description = "Invalid, expired, or unverified Google ID token")
	})
	public GoogleAuthResponse googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
		return googleAuthService.authenticate(request.idToken());
	}

	@GetMapping("/me")
	@Operation(summary = "Get the current user", description = "Requires a valid JWT.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Current user"),
			@ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
	})
	public UserResponse currentUser(@AuthenticationPrincipal CurrentUser currentUser) {
		return authService.getCurrentUser(currentUser.userId());
	}
}
