package com.switchmate.resume;

public interface ResumeTextExtractor {

	String contentType();

	String extract(byte[] fileContent);
}