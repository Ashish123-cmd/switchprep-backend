package com.switchmate.security;

import java.util.UUID;

public interface TokenService {

	String issue(UUID userId);

	UUID parse(String token);
}