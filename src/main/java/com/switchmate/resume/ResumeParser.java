package com.switchmate.resume;

public interface ResumeParser {

	ParsedProfile parse(String resumeText);
}