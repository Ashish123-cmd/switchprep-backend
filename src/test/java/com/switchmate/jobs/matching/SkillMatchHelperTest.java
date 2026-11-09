package com.switchmate.jobs.matching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class SkillMatchHelperTest {

	private final SkillMatchHelper helper = new SkillMatchHelper();

	@Test
	void separatesMatchedAndMissingJobSkillsCaseInsensitively() {
		SkillBreakdown result = helper.compare(Set.of("java", "SQL"), Set.of("Java", "Spring Boot", "SQL"));

		assertEquals(Set.of("Java", "SQL"), Set.copyOf(result.matchedSkills()));
		assertEquals(Set.of("Spring Boot"), Set.copyOf(result.missingSkills()));
	}

	@Test
	void emptyAndDuplicateSkillsAreHandled() {
		SkillBreakdown result = helper.compare(Set.of(" "), new LinkedHashSet<>(List.of("Java", "java", "")));

		assertEquals(List.of(), result.matchedSkills());
		assertEquals(List.of("Java"), result.missingSkills());
	}
}