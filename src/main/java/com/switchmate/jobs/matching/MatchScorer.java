package com.switchmate.jobs.matching;

import com.switchmate.domain.entity.Job;

public interface MatchScorer {

	MatchResult score(UserProfile profile, JobPreferences prefs, Job job);
}