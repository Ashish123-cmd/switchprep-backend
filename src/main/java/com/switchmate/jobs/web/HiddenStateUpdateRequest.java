package com.switchmate.jobs.web;

import jakarta.validation.constraints.NotNull;

public record HiddenStateUpdateRequest(@NotNull Boolean hidden) {
}