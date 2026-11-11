package com.switchmate.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.switchmate.common.exception.BadRequestException;
import com.switchmate.common.exception.ResumeParsingException;
import com.switchmate.common.exception.SignupDraftExpiredException;
import com.switchmate.domain.entity.SignupDraft;
import com.switchmate.repository.SignupDraftRepository;
import com.switchmate.resume.ParsedProfile;
import com.switchmate.resume.ParsedEducation;
import com.switchmate.resume.ParsedExperience;
import com.switchmate.resume.ResumeParser;
import com.switchmate.resume.ResumeTextExtractorRegistry;
import com.switchmate.storage.FileStorage;

class SignupDraftServiceTest {

	@Mock
	private SignupDraftRepository draftRepository;
	@Mock
	private FileStorage fileStorage;
	@Mock
	private ResumeTextExtractorRegistry extractorRegistry;
	@Mock
	private ResumeParser resumeParser;
	@Mock
	private ObjectMapper objectMapper;

	private SignupDraftService service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		service = new SignupDraftService(draftRepository, fileStorage, extractorRegistry, resumeParser, objectMapper, 30);
	}

	@Test
	void createDraft_parsesAndStoresDraft() throws Exception {
		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getSize()).thenReturn(1024L);
		when(file.getContentType()).thenReturn("application/pdf");
		when(file.getBytes()).thenReturn("resume content".getBytes());

		when(extractorRegistry.extract("application/pdf", "resume content".getBytes())).thenReturn("extracted text");

		ParsedProfile parsedProfile = new ParsedProfile(
				"John Doe",
				"+1 555 1234",
				"San Francisco, CA",
				"Software Engineer",
				"Experienced engineer",
				5,
				List.of("Java", "Spring"),
				List.of(new ParsedExperience("Acme Corp", "Engineer",
						LocalDate.of(2020, 1, 1), LocalDate.of(2023, 1, 1), "Built stuff")),
				List.of(new ParsedEducation("Stanford", "BS", "CS", 2016, 2020)));

		when(resumeParser.parse("extracted text")).thenReturn(parsedProfile);
		when(fileStorage.saveDraft(any(String.class), any(MultipartFile.class))).thenReturn(java.nio.file.Path.of("test"));
		when(objectMapper.writeValueAsString(parsedProfile)).thenReturn("{}");

		SignupDraft savedDraft = new SignupDraft();
		savedDraft.setId(UUID.randomUUID());
		savedDraft.setFileName("draft-test.pdf");
		savedDraft.setParsedProfile("{}");
		savedDraft.setExpiresAt(Instant.now().plus(Duration.ofMinutes(30)));
		when(draftRepository.save(any(SignupDraft.class))).thenReturn(savedDraft);

		SignupDraftService.SignupDraftResponse response = service.createDraft(file);

		assertNotNull(response);
		assertEquals(parsedProfile, response.parsedProfile());
		assertNotNull(response.draftId());
		assertNotNull(response.expiresAt());
		verify(draftRepository).save(any(SignupDraft.class));
		verify(fileStorage).saveDraft(any(String.class), any(MultipartFile.class));
	}

	@Test
	void createDraft_rejectsEmptyFile() {
		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(true);

		assertThrows(BadRequestException.class, () -> service.createDraft(file));
	}

	@Test
	void createDraft_rejectsOversizedFile() {
		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getSize()).thenReturn(6L * 1024 * 1024);

		assertThrows(BadRequestException.class, () -> service.createDraft(file));
	}

	@Test
	void createDraft_rejectsInvalidContentType() {
		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getSize()).thenReturn(1024L);
		when(file.getContentType()).thenReturn("text/plain");

		assertThrows(BadRequestException.class, () -> service.createDraft(file));
	}

	@Test
	void createDraft_wrapsParsingException() throws Exception {
		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getSize()).thenReturn(1024L);
		when(file.getContentType()).thenReturn("application/pdf");
		when(file.getBytes()).thenReturn("resume content".getBytes());

		when(extractorRegistry.extract("application/pdf", "resume content".getBytes())).thenReturn("extracted text");
		when(resumeParser.parse("extracted text")).thenThrow(new ResumeParsingException("Parser failed"));

		assertThrows(ResumeParsingException.class, () -> service.createDraft(file));
	}

	@Test
	void getDraft_returnsDraft() throws Exception {
		UUID draftId = UUID.randomUUID();
		ParsedProfile parsedProfile = new ParsedProfile(
				"John Doe", "+1 555 1234", "San Francisco, CA", "Software Engineer", "Experienced engineer",
				5, List.of("Java"), List.of(), List.of());

		SignupDraft draft = new SignupDraft();
		draft.setId(draftId);
		draft.setFileName("draft-test.pdf");
		draft.setParsedProfile("{}");
		draft.setExpiresAt(Instant.now().plus(Duration.ofMinutes(30)));

		when(draftRepository.findById(draftId)).thenReturn(Optional.of(draft));
		when(objectMapper.readValue("{}", ParsedProfile.class)).thenReturn(parsedProfile);

		SignupDraftService.SignupDraftResponse response = service.getDraft(draftId);

		assertEquals(draftId, response.draftId());
		assertEquals(parsedProfile, response.parsedProfile());
	}

	@Test
	void getDraft_throwsWhenExpired() {
		UUID draftId = UUID.randomUUID();
		SignupDraft draft = new SignupDraft();
		draft.setId(draftId);
		draft.setFileName("draft-test.pdf");
		draft.setParsedProfile("{}");
		draft.setExpiresAt(Instant.now().minusSeconds(10));

		when(draftRepository.findById(draftId)).thenReturn(Optional.of(draft));

		assertThrows(SignupDraftExpiredException.class, () -> service.getDraft(draftId));
	}

	@Test
	void consumeDraft_returnsParsedProfileAndDeletesDraft() throws Exception {
		UUID draftId = UUID.randomUUID();
		SignupDraft draft = draft(draftId, Instant.now().plus(Duration.ofMinutes(30)));
		ParsedProfile parsedProfile = new ParsedProfile(
				"John Doe", "+1 555 1234", "San Francisco, CA", "Software Engineer", "Experienced engineer",
				5, List.of("Java"), List.of(), List.of());

		when(draftRepository.findById(draftId)).thenReturn(Optional.of(draft));
		when(objectMapper.readValue("{}", ParsedProfile.class)).thenReturn(parsedProfile);

		SignupDraftService.ConsumedDraft consumed = service.consumeDraft(draftId);

		assertEquals(parsedProfile, consumed.parsedProfile());
		assertEquals("draft-test.pdf", consumed.fileName());
		verify(draftRepository).delete(draft);
	}

	@Test
	void consumeDraft_throwsWhenMissing() {
		UUID draftId = UUID.randomUUID();
		when(draftRepository.findById(draftId)).thenReturn(Optional.empty());

		assertThrows(SignupDraftExpiredException.class, () -> service.consumeDraft(draftId));
	}

	@Test
	void consumeDraft_throwsWhenExpired() {
		UUID draftId = UUID.randomUUID();
		SignupDraft draft = draft(draftId, Instant.now().minusSeconds(10));
		when(draftRepository.findById(draftId)).thenReturn(Optional.of(draft));

		assertThrows(SignupDraftExpiredException.class, () -> service.consumeDraft(draftId));
		verify(draftRepository).delete(draft);
	}

	@Test
	void deleteExpiredDrafts_deletesExpired() {
		service.deleteExpiredDrafts();
		verify(draftRepository).deleteByExpiresAtBefore(any(Instant.class));
	}

	private SignupDraft draft(UUID draftId, Instant expiresAt) {
		SignupDraft draft = new SignupDraft();
		draft.setId(draftId);
		draft.setFileName("draft-test.pdf");
		draft.setParsedProfile("{}");
		draft.setExpiresAt(expiresAt);
		return draft;
	}
}