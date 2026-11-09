package com.switchmate.auth.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.auth.dto.StandardAnswersRequest;
import com.switchmate.auth.dto.StandardAnswersResponse;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.StandardAnswer;
import com.switchmate.repository.StandardAnswerRepository;

@Service
public class StandardAnswersService {

	private final StandardAnswerRepository standardAnswerRepository;

	public StandardAnswersService(StandardAnswerRepository standardAnswerRepository) {
		this.standardAnswerRepository = standardAnswerRepository;
	}

	@Transactional(readOnly = true)
	public StandardAnswersResponse get(UUID userId) {
		return toResponse(find(userId));
	}

	@Transactional
	public StandardAnswersResponse replace(UUID userId, StandardAnswersRequest request) {
		StandardAnswer answers = find(userId);
		answers.setRequiresSponsorship(request.requiresSponsorship());
		answers.setWillingToRelocate(request.willingToRelocate());
		answers.setLinkedinUrl(request.linkedinUrl());
		answers.setGithubUrl(request.githubUrl());
		answers.setPortfolioUrl(request.portfolioUrl());
		answers.setCustomAnswers(request.customAnswers() == null
				? new LinkedHashMap<>() : new LinkedHashMap<>(request.customAnswers()));
		return toResponse(standardAnswerRepository.save(answers));
	}

	private StandardAnswer find(UUID userId) {
		return standardAnswerRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Standard answers not found"));
	}

	private StandardAnswersResponse toResponse(StandardAnswer answers) {
		Map<String, String> customAnswers = Map.copyOf(answers.getCustomAnswers());
		return new StandardAnswersResponse(answers.getRequiresSponsorship(), answers.getWillingToRelocate(),
				answers.getLinkedinUrl(), answers.getGithubUrl(), answers.getPortfolioUrl(), customAnswers);
	}
}