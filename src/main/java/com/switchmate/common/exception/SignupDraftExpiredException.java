package com.switchmate.common.exception;

public class SignupDraftExpiredException extends RuntimeException {

	public SignupDraftExpiredException(String message) {
		super(message);
	}
}