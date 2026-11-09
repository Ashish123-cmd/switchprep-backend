package com.switchmate.jobs.ingestion;

import java.util.Set;

public interface SkillExtractor {

	Set<String> extract(String title, String description);
}