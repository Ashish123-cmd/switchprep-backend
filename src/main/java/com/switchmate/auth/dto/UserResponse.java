package com.switchmate.auth.dto;

import java.util.UUID;

public record UserResponse(UUID id, String name, String email, ProfileResponse profile) {
}