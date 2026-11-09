package com.switchmate.resume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.switchmate.common.exception.BadRequestException;

class ResumeTextExtractorRegistryTest {

	@Test
	void dispatchesByNormalizedContentType() {
		ResumeTextExtractorRegistry registry = new ResumeTextExtractorRegistry(List.of(
				extractor(PdfBoxResumeTextExtractor.CONTENT_TYPE, "pdf text"),
				extractor(ApachePoiResumeTextExtractor.CONTENT_TYPE, "docx text")));

		assertEquals("pdf text", registry.extract("APPLICATION/PDF; charset=binary", new byte[0]));
		assertEquals("docx text", registry.extract(ApachePoiResumeTextExtractor.CONTENT_TYPE, new byte[0]));
	}

	@Test
	void rejectsUnregisteredContentType() {
		ResumeTextExtractorRegistry registry = new ResumeTextExtractorRegistry(List.of(
				extractor(PdfBoxResumeTextExtractor.CONTENT_TYPE, "pdf text")));

		assertThrows(BadRequestException.class, () -> registry.extract("text/plain", new byte[0]));
	}

	private ResumeTextExtractor extractor(String contentType, String text) {
		return new ResumeTextExtractor() {
			@Override
			public String contentType() {
				return contentType;
			}

			@Override
			public String extract(byte[] fileContent) {
				return text;
			}
		};
	}
}