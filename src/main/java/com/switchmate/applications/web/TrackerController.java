package com.switchmate.applications.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.switchmate.applications.ApplicationService;
import com.switchmate.applications.dto.TrackerItem;
import com.switchmate.security.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tracker")
@Tag(name = "Tracker", description = "Applications and saved jobs")
public class TrackerController {

	private final ApplicationService applicationService;

	public TrackerController(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@GetMapping
	public List<TrackerItem> getTracker(@AuthenticationPrincipal CurrentUser currentUser) {
		return applicationService.tracker(currentUser.userId());
	}
}