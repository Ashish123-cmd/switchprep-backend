package com.switchmate.auth.dto;

public record GoogleAuthResponse(String token, UserResponse user, boolean isNewUser) {
}
