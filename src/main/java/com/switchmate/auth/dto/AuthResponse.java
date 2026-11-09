package com.switchmate.auth.dto;

public record AuthResponse(String token, UserResponse user) {
}