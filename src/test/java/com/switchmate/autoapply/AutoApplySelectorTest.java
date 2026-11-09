package com.switchmate.autoapply;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AutoApplySelectorTest {

	private final AutoApplySelector selector = new AutoApplySelector();

	@Test
	void disabledOrExhaustedRunSelectsNothing() {
		List<AutoApplyCandidate> candidates = List.of(candidate("A", 99));

		assertTrue(selector.select(false, candidates, 0, 10, Set.of()).isEmpty());
		assertTrue(selector.select(true, candidates, 0, 0, Set.of()).isEmpty());
	}

	@Test
	void appliesAllEligibilityRulesAndExcludesCompanyCaseInsensitively() {
		List<AutoApplyCandidate> candidates = List.of(
				candidate("eligible", "Good Co", 75, true, false, false),
				candidate("low-score", "Good Co", 49, true, false, false),
				candidate("hidden", "Good Co", 95, true, true, false),
				candidate("inactive", "Good Co", 99, false, false, false),
				candidate("applied", "Good Co", 98, true, false, true),
				candidate("excluded", "Bad Company", 100, true, false, false));

		List<AutoApplyCandidate> selected = selector.select(true, candidates, 50, 20, Set.of(" bad company "));

		assertEquals(List.of(jobId("eligible")), selected.stream().map(AutoApplyCandidate::jobId).toList());
	}

	@Test
	void highestScoresAreSelectedFirstWithinRemainingDailyCap() {
		List<AutoApplyCandidate> candidates = List.of(candidate("low", 70), candidate("high", 95), candidate("mid", 80));

		List<AutoApplyCandidate> selected = selector.select(true, candidates, 70, 2, Set.of());

		assertEquals(List.of(jobId("high"), jobId("mid")), selected.stream()
				.map(AutoApplyCandidate::jobId).toList());
	}

	@Test
	void duplicateJobIdsAreReturnedOnlyOnce() {
		AutoApplyCandidate duplicate = candidate("same", "Example", 90, true, false, false);

		assertEquals(1, selector.select(true, List.of(duplicate, duplicate), 0, 5, Set.of()).size());
	}

	private AutoApplyCandidate candidate(String id, int score) {
		return candidate(id, "Example", score, true, false, false);
	}

	private UUID jobId(String id) {
		return UUID.nameUUIDFromBytes(id.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private AutoApplyCandidate candidate(String id, String company, int score, boolean active,
			boolean hidden, boolean alreadyApplied) {
		return new AutoApplyCandidate(jobId(id),
				company, score, active, hidden, alreadyApplied);
	}
}