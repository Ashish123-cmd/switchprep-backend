package com.switchmate.resume;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import com.switchmate.common.exception.ResumeParsingException;

@Component
public class PdfBoxResumeTextExtractor implements ResumeTextExtractor {

	public static final String CONTENT_TYPE = "application/pdf";

	@Override
	public String contentType() {
		return CONTENT_TYPE;
	}

	@Override
	public String extract(byte[] fileContent) {
		try (PDDocument document = Loader.loadPDF(fileContent)) {
			return new PDFTextStripper().getText(document);
		} catch (IOException exception) {
			throw new ResumeParsingException("The uploaded PDF could not be read", exception);
		}
	}
}