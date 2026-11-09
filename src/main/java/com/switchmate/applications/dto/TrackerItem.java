package com.switchmate.applications.dto;

import java.time.Instant;
import java.util.UUID;

import com.switchmate.domain.enums.ApplicationMode;

public record TrackerItem(
		UUID applicationId,
		UUID jobId,
		String status,
		ApplicationMode mode,
		Instant updatedAt,
		boolean saved,
		CompactJobResponse job) {
}