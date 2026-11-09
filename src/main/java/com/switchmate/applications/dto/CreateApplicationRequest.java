package com.switchmate.applications.dto;

import java.util.UUID;

import com.switchmate.domain.enums.ApplicationMode;

import jakarta.validation.constraints.NotNull;

public record CreateApplicationRequest(
		@NotNull UUID jobId,
		@NotNull ApplicationMode mode) {
}