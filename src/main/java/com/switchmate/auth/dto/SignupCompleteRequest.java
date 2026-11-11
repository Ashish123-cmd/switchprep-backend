package com.switchmate.auth.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupCompleteRequest(
		@NotNull UUID draftId,
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Email @Size(max = 320) String email,
		@NotBlank @Size(min = 8) String password) {
}