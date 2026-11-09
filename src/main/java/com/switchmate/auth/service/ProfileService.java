package com.switchmate.auth.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.auth.dto.EducationRequest;
import com.switchmate.auth.dto.EducationResponse;
import com.switchmate.auth.dto.ExperienceRequest;
import com.switchmate.auth.dto.ExperienceResponse;
import com.switchmate.auth.dto.UserProfileRequest;
import com.switchmate.auth.dto.UserProfileResponse;
import com.switchmate.common.exception.BadRequestException;
import com.switchmate.common.exception.ConflictException;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.Education;
import com.switchmate.domain.entity.Experience;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.StandardAnswer;
import com.switchmate.domain.entity.User;
import com.switchmate.repository.EducationRepository;
import com.switchmate.repository.ExperienceRepository;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.repository.StandardAnswerRepository;
import com.switchmate.repository.UserRepository;

@Service
public class ProfileService {

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final ExperienceRepository experienceRepository;
	private final EducationRepository educationRepository;
	private final StandardAnswerRepository standardAnswerRepository;

	public ProfileService(
			UserRepository userRepository,
			ProfileRepository profileRepository,
			ExperienceRepository experienceRepository,
			EducationRepository educationRepository,
			StandardAnswerRepository standardAnswerRepository) {
		this.userRepository = userRepository;
		this.profileRepository = profileRepository;
		this.experienceRepository = experienceRepository;
		this.educationRepository = educationRepository;
		this.standardAnswerRepository = standardAnswerRepository;
	}

	@Transactional(readOnly = true)
	public UserProfileResponse get(UUID userId) {
		User user = findUser(userId);
		Profile profile = findProfile(userId);
		return toResponse(user, profile,
				experienceRepository.findAllByProfileId(userId),
				educationRepository.findAllByProfileId(userId));
	}

	@Transactional
	public UserProfileResponse replace(UUID userId, UserProfileRequest request) {
		User user = findUser(userId);
		Profile profile = findProfile(userId);
		String email = request.email().strip().toLowerCase(java.util.Locale.ROOT);
		if (userRepository.existsByEmailAndIdNot(email, userId)) {
			throw new ConflictException("Email is already registered");
		}
		user.setFullName(request.name().strip());
		user.setEmail(email);
		userRepository.save(user);

		profile.setPhone(request.phone());
		profile.setLocation(request.location());
		profile.setHeadline(request.headline());
		profile.setSummary(request.summary());
		profile.setTotalExperienceYears(request.totalExperienceYears());
		profile.setSkills(new LinkedHashSet<>(request.skills()));
		profile.setResumeFileName(request.resumeFileName());
		profile.setResumeUploadedAt(request.resumeUploadedAt());
		profileRepository.save(profile);

		experienceRepository.deleteAllByProfileId(userId);
		educationRepository.deleteAllByProfileId(userId);
		List<Experience> experiences = request.experiences().stream()
				.map(item -> toExperience(userId, item))
				.toList();
		List<Education> educations = request.educations().stream()
				.map(item -> toEducation(userId, item))
				.toList();
		experiences = experienceRepository.saveAll(experiences);
		educations = educationRepository.saveAll(educations);
		return toResponse(user, profile, experiences, educations);
	}

	@Transactional
	public UserProfileResponse complete(UUID userId) {
		User user = findUser(userId);
		Profile profile = findProfile(userId);
		StandardAnswer answers = standardAnswerRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Standard answers not found"));
		if (user.getFullName() == null || user.getFullName().isBlank()
				|| user.getEmail() == null || user.getEmail().isBlank()
				|| profile.getSkills().isEmpty()
				|| answers.getRequiresSponsorship() == null) {
			throw new BadRequestException("Name, email, at least one skill, and sponsorship answer are required");
		}
		profile.setOnboardingCompleted(true);
		profileRepository.save(profile);
		return get(userId);
	}

	private User findUser(UUID userId) {
		return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
	}

	private Profile findProfile(UUID userId) {
		return profileRepository.findById(userId).orElseThrow(() -> new NotFoundException("Profile not found"));
	}

	private Experience toExperience(UUID userId, ExperienceRequest request) {
		return new Experience(userId, request.company(), request.title(), request.startDate(), request.endDate(),
				request.description());
	}

	private Education toEducation(UUID userId, EducationRequest request) {
		return new Education(userId, request.institution(), request.degree(), request.field(), request.startYear(),
				request.endYear());
	}

	private UserProfileResponse toResponse(
			User user,
			Profile profile,
			List<Experience> experiences,
			List<Education> educations) {
		return new UserProfileResponse(
				user.getFullName(),
				user.getEmail(),
				profile.isOnboardingCompleted(),
				profile.getPhone(),
				profile.getLocation(),
				profile.getHeadline(),
				profile.getSummary(),
				profile.getTotalExperienceYears(),
				Set.copyOf(profile.getSkills()),
				experiences.stream().map(this::toResponse).toList(),
				educations.stream().map(this::toResponse).toList(),
				profile.getResumeFileName(),
				profile.getResumeUploadedAt());
	}

	private ExperienceResponse toResponse(Experience experience) {
		return new ExperienceResponse(experience.getId(), experience.getCompany(), experience.getTitle(),
				experience.getStartDate(), experience.getEndDate(), experience.getDescription());
	}

	private EducationResponse toResponse(Education education) {
		return new EducationResponse(education.getId(), education.getInstitution(), education.getDegree(),
				education.getField(), education.getStartYear(), education.getEndYear());
	}
}