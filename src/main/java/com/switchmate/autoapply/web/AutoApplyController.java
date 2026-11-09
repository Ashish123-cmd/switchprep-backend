package com.switchmate.autoapply.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.switchmate.autoapply.AutoApplyService;
import com.switchmate.autoapply.AutoApplySettingsService;
import com.switchmate.autoapply.dto.AutoApplyRunSummary;
import com.switchmate.autoapply.dto.AutoApplySettingsRequest;
import com.switchmate.autoapply.dto.AutoApplySettingsResponse;
import com.switchmate.security.CurrentUser;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auto-apply")
@Tag(name = "Auto Apply", description = "Automatic application settings and runs")
public class AutoApplyController {

	private final AutoApplySettingsService settingsService;
	private final AutoApplyService autoApplyService;

	public AutoApplyController(AutoApplySettingsService settingsService, AutoApplyService autoApplyService) {
		this.settingsService = settingsService;
		this.autoApplyService = autoApplyService;
	}

	@GetMapping("/settings")
	public AutoApplySettingsResponse getSettings(@AuthenticationPrincipal CurrentUser currentUser) {
		return settingsService.get(currentUser.userId());
	}

	@PutMapping("/settings")
	public AutoApplySettingsResponse replaceSettings(
			@AuthenticationPrincipal CurrentUser currentUser,
			@Valid @RequestBody AutoApplySettingsRequest request) {
		return settingsService.replace(currentUser.userId(), request);
	}

	@PostMapping("/run")
	public AutoApplyRunSummary run(@AuthenticationPrincipal CurrentUser currentUser) {
		return autoApplyService.run(currentUser.userId());
	}
}