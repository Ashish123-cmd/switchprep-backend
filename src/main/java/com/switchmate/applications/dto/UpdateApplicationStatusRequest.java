package com.switchmate.applications.dto;

import com.switchmate.domain.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(@NotNull ApplicationStatus status) {
}