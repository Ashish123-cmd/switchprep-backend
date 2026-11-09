package com.switchmate.resume;

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.switchmate.common.exception.BadRequestException;

@Component
public class ResumeTextExtractorRegistry {

	private final Map<String, ResumeTextExtractor> extractors;

	public ResumeTextExtractorRegistry(List<ResumeTextExtractor> extractors) {
		this.extractors = extractors.stream().collect(Collectors.toUnmodifiableMap(
						extractor -> normalize(extractor.contentType()), Function.identity()));
	}

	public String extract(String contentType, byte[] fileContent) {
		ResumeTextExtractor extractor = extractors.get(normalize(contentType));
		if (extractor == null) {
			throw new BadRequestException("Resume must be a PDF or DOCX file");
		}
		return extractor.extract(fileContent);
	}

	private static String normalize(String contentType) {
		if (contentType == null) {
			return "";
		}
		int parameters = contentType.indexOf(';');
		String mediaType = parameters >= 0 ? contentType.substring(0, parameters) : contentType;
		return mediaType.strip().toLowerCase(Locale.ROOT);
	}
}