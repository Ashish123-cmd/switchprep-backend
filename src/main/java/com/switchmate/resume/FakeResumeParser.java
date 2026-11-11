package com.switchmate.resume;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("fake-ai")
public class FakeResumeParser implements ResumeParser {

	@Override
	public ParsedProfile parse(String resumeText) {
		return new ParsedProfile(
				"Alex Morgan",
				"+1 555 010 1234",
				"Seattle, WA",
				"Software Engineer",
				"Software engineer experienced in building reliable web applications.",
				5,
				List.of("Java", "Spring Boot", "PostgreSQL"),
				List.of(new ParsedExperience("Example Company", "Software Engineer",
						LocalDate.of(2021, 3, 1), LocalDate.of(2024, 6, 30),
						"Built and maintained backend services.")),
				List.of(new ParsedEducation("Example University", "BS", "Computer Science", 2016, 2020)));
	}
}