package com.switchmate.applications.dto;

import java.time.Instant;
import java.util.UUID;

import com.switchmate.domain.enums.ApplicationMode;
import com.switchmate.domain.enums.ApplicationStatus;

public record ApplicationResponse(
		UUID id,
		UUID jobId,
		ApplicationStatus status,
		ApplicationMode mode,
		Instant updatedAt) {
}