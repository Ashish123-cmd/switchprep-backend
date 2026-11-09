package com.switchmate.resume;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import com.switchmate.common.exception.ResumeParsingException;

@Component
public class ApachePoiResumeTextExtractor implements ResumeTextExtractor {

	public static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

	@Override
	public String contentType() {
		return CONTENT_TYPE;
	}

	@Override
	public String extract(byte[] fileContent) {
		try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(fileContent));
				XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
			return extractor.getText();
		} catch (IOException exception) {
			throw new ResumeParsingException("The uploaded DOCX could not be read", exception);
		}
	}
}