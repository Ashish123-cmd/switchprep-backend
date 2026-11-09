package com.switchmate.jobs.ingestion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class ExperienceRangeExtractor {

	private static final Pattern RANGE_PATTERN = Pattern.compile(
			"(?i)(\\d{1,2})\\s*[-–]\\s*(\\d{1,2})\\s*(?:years?|yrs?)\\b");
	private static final Pattern MINIMUM_PATTERN = Pattern.compile(
			"(?i)(\\d{1,2})\\s*\\+\\s*(?:years?|yrs?)\\b");

	public ExperienceRange extract(String text) {
		if (text == null || text.isBlank()) {
			return null;
		}
		Matcher range = RANGE_PATTERN.matcher(text);
		if (range.find()) {
			return new ExperienceRange(Integer.valueOf(range.group(1)), Integer.valueOf(range.group(2)));
		}
		Matcher minimum = MINIMUM_PATTERN.matcher(text);
		if (minimum.find()) {
			return new ExperienceRange(Integer.valueOf(minimum.group(1)), null);
		}
		return null;
	}
}