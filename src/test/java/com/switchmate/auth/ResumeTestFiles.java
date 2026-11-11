package com.switchmate.auth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

final class ResumeTestFiles {

	private ResumeTestFiles() {
	}

	static byte[] pdfWithText(String text) throws IOException {
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage();
			document.addPage(page);
			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				contentStream.beginText();
				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				contentStream.newLineAtOffset(72, 720);
				contentStream.showText(text);
				contentStream.endText();
			}
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			document.save(output);
			return output.toByteArray();
		}
	}
}