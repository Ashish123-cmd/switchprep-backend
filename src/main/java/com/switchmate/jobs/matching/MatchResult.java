package com.switchmate.jobs.matching;

import java.util.List;

public record MatchResult(int score, List<String> reasons) {
}