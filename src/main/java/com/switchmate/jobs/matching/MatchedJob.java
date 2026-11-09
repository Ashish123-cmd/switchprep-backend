package com.switchmate.jobs.matching;

import java.util.List;

public record MatchedJob(JobResponse job, int score, List<String> reasons, boolean saved) {
}