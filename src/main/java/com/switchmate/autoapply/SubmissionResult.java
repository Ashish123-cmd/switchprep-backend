package com.switchmate.autoapply;

public record SubmissionResult(boolean success, String message) {

	public static SubmissionResult succeeded() {
		return new SubmissionResult(true, null);
	}

	public static SubmissionResult failed(String message) {
		return new SubmissionResult(false, message);
	}
}