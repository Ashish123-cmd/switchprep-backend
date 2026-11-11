package com.switchmate.auth.service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.switchmate.common.exception.BadRequestException;
import com.switchmate.common.exception.ResumeParsingException;
import com.switchmate.common.exception.SignupDraftExpiredException;
import com.switchmate.domain.entity.SignupDraft;
import com.switchmate.repository.SignupDraftRepository;
import com.switchmate.resume.ParsedProfile;
import com.switchmate.resume.ResumeParser;
import com.switchmate.resume.ResumeTextExtractorRegistry;
import com.switchmate.storage.FileStorage;

@Service
public class SignupDraftService {

	private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

	private final SignupDraftRepository draftRepository;
	private final FileStorage fileStorage;
	private final ResumeTextExtractorRegistry extractorRegistry;
	private final ResumeParser resumeParser;
	private final ObjectMapper objectMapper;
	private final Duration draftTtl;

	public SignupDraftService(
			SignupDraftRepository draftRepository,
			FileStorage fileStorage,
			ResumeTextExtractorRegistry extractorRegistry,
			ResumeParser resumeParser,
			ObjectMapper objectMapper,
			@org.springframework.beans.factory.annotation.Value("${app.auth.signup-draft.ttl-minutes:30}") long ttlMinutes) {
		this.draftRepository = draftRepository;
		this.fileStorage = fileStorage;
		this.extractorRegistry = extractorRegistry;
		this.resumeParser = resumeParser;
		this.objectMapper = objectMapper;
		this.draftTtl = Duration.ofMinutes(ttlMinutes);
	}

	@Transactional
	public SignupDraftResponse createDraft(MultipartFile file, String clientIp) {
		validateFile(file);
		
		String resumeText;
		try {
			resumeText = extractorRegistry.extract(file.getContentType(), file.getBytes());
		} catch (IOException exception) {
			throw new BadRequestException("Could not read the uploaded resume file");
		}
		
		ParsedProfile parsedProfile;
		try {
			parsedProfile = resumeParser.parse(resumeText);
		} catch (ResumeParsingException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new ResumeParsingException("Failed to parse resume", exception);
		}
		
		String generatedName = "draft-" + UUID.randomUUID() + getExtension(file.getContentType());
		fileStorage.saveDraft(generatedName, file);
		
		Instant now = Instant.now();
		Instant expiresAt = now.plus(draftTtl);
		
		SignupDraft draft = new SignupDraft();
		draft.setId(UUID.randomUUID());
		draft.setFileName(generatedName);
		draft.setParsedProfile(serialize(parsedProfile));
		draft.setExpiresAt(expiresAt);
		
		draftRepository.save(draft);
		
		return new SignupDraftResponse(draft.getId(), parsedProfile, expiresAt);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("Resume file is required");
		}
		if (file.getSize() > MAX_FILE_SIZE_BYTES) {
			throw new BadRequestException("Resume file must not exceed 5 MB");
		}
		String contentType = file.getContentType();
		String mediaType = contentType == null ? "" : contentType.split(";", 2)[0].strip();
		if (!(mediaType.equalsIgnoreCase("application/pdf")
				|| mediaType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
			throw new BadRequestException("Resume must be a PDF or DOCX file");
		}
	}

	private String getExtension(String contentType) {
		if (contentType == null) {
			return ".pdf";
		}
		String mediaType = contentType.split(";", 2)[0].strip().toLowerCase();
		return switch (mediaType) {
			case "application/pdf" -> ".pdf";
			case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
			default -> ".pdf";
		};
	}

	@Transactional
	public SignupDraftResponse getDraft(UUID draftId) {
		SignupDraft draft = draftRepository.findById(draftId)
				.orElseThrow(() -> new SignupDraftExpiredException("Draft not found or expired"));
		
		if (draft.getExpiresAt().isBefore(Instant.now())) {
			draftRepository.delete(draft);
			throw new SignupDraftExpiredException("Draft not found or expired");
		}
		
		ParsedProfile parsedProfile = deserialize(draft.getParsedProfile());
		return new SignupDraftResponse(draft.getId(), parsedProfile, draft.getExpiresAt());
	}

	@Transactional
	public ConsumedDraft consumeDraft(UUID draftId) {
		SignupDraft draft = draftRepository.findById(draftId)
				.orElseThrow(() -> new SignupDraftExpiredException("Draft not found or expired"));
		
		if (draft.getExpiresAt().isBefore(Instant.now())) {
			draftRepository.delete(draft);
			throw new SignupDraftExpiredException("Draft not found or expired");
		}
		
		ParsedProfile parsedProfile = deserialize(draft.getParsedProfile());
		String fileName = draft.getFileName();
		
		draftRepository.delete(draft);
		
		return new ConsumedDraft(parsedProfile, fileName);
	}

	@Transactional
	public void deleteExpiredDrafts() {
		draftRepository.deleteByExpiresAtBefore(Instant.now());
	}

	public record SignupDraftResponse(
			UUID draftId,
			ParsedProfile parsedProfile,
			Instant expiresAt) {
	}

	public record ConsumedDraft(
			ParsedProfile parsedProfile,
			String fileName) {
	}

	private String serialize(ParsedProfile parsedProfile) {
		try {
			return objectMapper.writeValueAsString(parsedProfile);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not store the parsed signup profile", exception);
		}
	}

	private ParsedProfile deserialize(String parsedProfile) {
		try {
			return objectMapper.readValue(parsedProfile, ParsedProfile.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Stored signup draft is invalid", exception);
		}
	}
}