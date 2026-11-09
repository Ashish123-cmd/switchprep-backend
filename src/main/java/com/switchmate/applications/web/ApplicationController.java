package com.switchmate.applications.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.switchmate.applications.ApplicationService;
import com.switchmate.applications.dto.ApplicationView;
import com.switchmate.applications.dto.CreateApplicationRequest;
import com.switchmate.applications.dto.UpdateApplicationStatusRequest;
import com.switchmate.domain.enums.ApplicationMode;
import com.switchmate.domain.enums.ApplicationStatus;
import com.switchmate.security.CurrentUser;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Application queue and status management")
public class ApplicationController {

	private final ApplicationService applicationService;

	public ApplicationController(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApplicationView create(
			@AuthenticationPrincipal CurrentUser currentUser,
			@Valid @RequestBody CreateApplicationRequest request) {
		return applicationService.create(currentUser.userId(), request);
	}

	@GetMapping
	public List<ApplicationView> list(
			@AuthenticationPrincipal CurrentUser currentUser,
			@RequestParam(required = false) ApplicationStatus status,
			@RequestParam(required = false) ApplicationMode mode) {
		return applicationService.list(currentUser.userId(), status, mode);
	}

	@PatchMapping("/{id}/status")
	public ApplicationView updateStatus(
			@AuthenticationPrincipal CurrentUser currentUser,
			@PathVariable UUID id,
			@Valid @RequestBody UpdateApplicationStatusRequest request) {
		return applicationService.updateStatus(currentUser.userId(), id, request.status());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal CurrentUser currentUser, @PathVariable UUID id) {
		applicationService.delete(currentUser.userId(), id);
	}
}