package com.switchmate.auth.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.switchmate.auth.dto.JobPreferencesRequest;
import com.switchmate.auth.dto.JobPreferencesResponse;
import com.switchmate.auth.dto.StandardAnswersRequest;
import com.switchmate.auth.dto.StandardAnswersResponse;
import com.switchmate.auth.dto.UserProfileRequest;
import com.switchmate.auth.dto.UserProfileResponse;
import com.switchmate.auth.service.PreferencesService;
import com.switchmate.auth.service.ProfileService;
import com.switchmate.auth.service.ResumeService;
import com.switchmate.auth.service.StandardAnswersService;
import com.switchmate.resume.ParsedProfile;
import com.switchmate.security.CurrentUser;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "Profile, preferences, resume, and onboarding")
public class ProfileController {

	private final ProfileService profileService;
	private final PreferencesService preferencesService;
	private final StandardAnswersService standardAnswersService;
	private final ResumeService resumeService;

	public ProfileController(
			ProfileService profileService,
			PreferencesService preferencesService,
			StandardAnswersService standardAnswersService,
			ResumeService resumeService) {
		this.profileService = profileService;
		this.preferencesService = preferencesService;
		this.standardAnswersService = standardAnswersService;
		this.resumeService = resumeService;
	}

	@PostMapping(path = "/resume", consumes = "multipart/form-data")
	public ParsedProfile uploadResume(
			@AuthenticationPrincipal CurrentUser currentUser,
			@RequestPart("file") MultipartFile file) {
		return resumeService.upload(currentUser.userId(), file);
	}

	@GetMapping
	public UserProfileResponse getProfile(@AuthenticationPrincipal CurrentUser currentUser) {
		return profileService.get(currentUser.userId());
	}

	@PutMapping
	public UserProfileResponse replaceProfile(
			@AuthenticationPrincipal CurrentUser currentUser,
			@Valid @RequestBody UserProfileRequest request) {
		return profileService.replace(currentUser.userId(), request);
	}

	@PostMapping("/complete")
	public UserProfileResponse complete(@AuthenticationPrincipal CurrentUser currentUser) {
		return profileService.complete(currentUser.userId());
	}

	@GetMapping("/preferences")
	public JobPreferencesResponse getPreferences(@AuthenticationPrincipal CurrentUser currentUser) {
		return preferencesService.get(currentUser.userId());
	}

	@PutMapping("/preferences")
	public JobPreferencesResponse replacePreferences(
			@AuthenticationPrincipal CurrentUser currentUser,
			@Valid @RequestBody JobPreferencesRequest request) {
		return preferencesService.replace(currentUser.userId(), request);
	}

	@GetMapping("/standard-answers")
	public StandardAnswersResponse getStandardAnswers(@AuthenticationPrincipal CurrentUser currentUser) {
		return standardAnswersService.get(currentUser.userId());
	}

	@PutMapping("/standard-answers")
	public StandardAnswersResponse replaceStandardAnswers(
			@AuthenticationPrincipal CurrentUser currentUser,
			@Valid @RequestBody StandardAnswersRequest request) {
		return standardAnswersService.replace(currentUser.userId(), request);
	}
}