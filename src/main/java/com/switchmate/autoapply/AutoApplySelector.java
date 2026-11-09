package com.switchmate.autoapply;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class AutoApplySelector {

	public List<AutoApplyCandidate> select(
			boolean enabled,
			List<AutoApplyCandidate> candidates,
			int minMatchScore,
			int remainingDailySlots,
			Set<String> excludedCompanies) {
		if (!enabled || remainingDailySlots <= 0) {
			return List.of();
		}
		Set<String> excluded = normalizeCompanies(excludedCompanies);
		List<AutoApplyCandidate> eligible = candidates.stream()
				.filter(AutoApplyCandidate::active)
				.filter(candidate -> !candidate.hidden())
				.filter(candidate -> !candidate.alreadyApplied())
				.filter(candidate -> candidate.score() >= minMatchScore)
				.filter(candidate -> !excluded.contains(normalize(candidate.company())))
				.sorted(Comparator.comparingInt(AutoApplyCandidate::score).reversed())
				.toList();

		List<AutoApplyCandidate> selected = new ArrayList<>();
		Set<UUID> selectedJobIds = new HashSet<>();
		for (AutoApplyCandidate candidate : eligible) {
			if (selected.size() >= remainingDailySlots) {
				break;
			}
			if (selectedJobIds.add(candidate.jobId())) {
				selected.add(candidate);
			}
		}
		return List.copyOf(selected);
	}

	private Set<String> normalizeCompanies(Set<String> companies) {
		if (companies == null) {
			return Set.of();
		}
		return companies.stream().filter(company -> company != null && !company.isBlank())
				.map(this::normalize).collect(java.util.stream.Collectors.toSet());
	}

	private String normalize(String value) {
		return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
	}
}