package com.switchmate.jobs.web;

import jakarta.validation.constraints.NotNull;

public record JobStateUpdateRequest(@NotNull Boolean saved) {
}