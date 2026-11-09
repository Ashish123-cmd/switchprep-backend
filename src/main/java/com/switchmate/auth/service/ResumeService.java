package com.switchmate.auth.service;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.switchmate.common.exception.BadRequestException;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.Profile;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.resume.ParsedProfile;
import com.switchmate.resume.ResumeParser;
import com.switchmate.resume.ResumeTextExtractorRegistry;
import com.switchmate.storage.FileStorage;

@Service
public class ResumeService {

	private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

	private final ProfileRepository profileRepository;
	private final FileStorage fileStorage;
	private final ResumeTextExtractorRegistry extractorRegistry;
	private final ResumeParser resumeParser;

	public ResumeService(
			ProfileRepository profileRepository,
			FileStorage fileStorage,
			ResumeTextExtractorRegistry extractorRegistry,
			ResumeParser resumeParser) {
		this.profileRepository = profileRepository;
		this.fileStorage = fileStorage;
		this.extractorRegistry = extractorRegistry;
		this.resumeParser = resumeParser;
	}

	@Transactional
	public ParsedProfile upload(UUID userId, MultipartFile file) {
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
		Profile profile = profileRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Profile not found"));

		String resumeText;
		try {
			resumeText = extractorRegistry.extract(contentType, file.getBytes());
		} catch (IOException exception) {
			throw new BadRequestException("Could not read the uploaded resume file");
		}
		ParsedProfile parsedProfile = resumeParser.parse(resumeText);
		String storedPath = fileStorage.save(userId, file);
		profile.setResumeFileName(storedPath);
		profile.setResumeUploadedAt(Instant.now());
		profileRepository.save(profile);
		return parsedProfile;
	}
}