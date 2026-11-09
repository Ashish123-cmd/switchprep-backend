package com.switchmate.applications.dto;

import java.time.Instant;
import java.util.UUID;

import com.switchmate.domain.enums.JobSource;

public record CompactJobResponse(
		UUID id,
		JobSource source,
		String title,
		String company,
		String location,
		String applyUrl,
		Instant postedAt) {
}