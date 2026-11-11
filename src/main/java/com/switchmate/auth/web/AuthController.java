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
import io.swagger.v3.oas.annotations.tags.Tag;

import com.switchmate.auth.dto.AuthResponse;
import com.switchmate.auth.dto.LoginRequest;
import com.switchmate.auth.dto.SignupCompleteRequest;
import com.switchmate.auth.dto.UserResponse;
import com.switchmate.auth.service.AuthService;
import com.switchmate.auth.service.SignupDraftService;
import com.switchmate.common.exception.RateLimitExceededException;
import com.switchmate.common.ratelimit.RateLimiter;
import com.switchmate.security.CurrentUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Signup, login, and current-user endpoints")
public class AuthController {

	private final AuthService authService;
	private final SignupDraftService signupDraftService;
	private final RateLimiter rateLimiter;

	public AuthController(AuthService authService, SignupDraftService signupDraftService, RateLimiter rateLimiter) {
		this.authService = authService;
		this.signupDraftService = signupDraftService;
		this.rateLimiter = rateLimiter;
	}

	@PostMapping(value = "/signup/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public SignupDraftService.SignupDraftResponse uploadSignupResume(
			@RequestPart("file") MultipartFile file,
			jakarta.servlet.http.HttpServletRequest request) {
		String clientIp = request.getRemoteAddr();
		if (!rateLimiter.tryAcquire(clientIp)) {
			throw new RateLimitExceededException("Too many resume upload attempts. Please try again later.");
		}
		return signupDraftService.createDraft(file, clientIp);
	}

	@PostMapping("/signup/complete")
	public AuthResponse completeSignup(@Valid @RequestBody SignupCompleteRequest request) {
		return authService.completeSignup(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	public UserResponse currentUser(@AuthenticationPrincipal CurrentUser currentUser) {
		return authService.getCurrentUser(currentUser.userId());
	}
}