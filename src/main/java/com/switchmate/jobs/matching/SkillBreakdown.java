package com.switchmate.jobs.matching;

import java.util.List;

public record SkillBreakdown(List<String> matchedSkills, List<String> missingSkills) {
}