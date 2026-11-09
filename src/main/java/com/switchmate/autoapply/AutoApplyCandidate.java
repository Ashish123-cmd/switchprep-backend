package com.switchmate.autoapply;

import java.util.UUID;

public record AutoApplyCandidate(
		UUID jobId,
		String company,
		int score,
		boolean active,
		boolean hidden,
		boolean alreadyApplied) {
}