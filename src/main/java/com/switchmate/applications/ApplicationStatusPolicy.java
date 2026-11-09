package com.switchmate.applications;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.switchmate.common.exception.BadRequestException;
import com.switchmate.domain.enums.ApplicationStatus;

@Component
public class ApplicationStatusPolicy {

	private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS = Map.of(
			ApplicationStatus.QUEUED, Set.of(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED),
			ApplicationStatus.APPLIED, Set.of(ApplicationStatus.QUEUED, ApplicationStatus.INTERVIEW,
					ApplicationStatus.REJECTED),
			ApplicationStatus.INTERVIEW, Set.of(ApplicationStatus.QUEUED, ApplicationStatus.APPLIED,
					ApplicationStatus.REJECTED),
			ApplicationStatus.REJECTED, Set.of(ApplicationStatus.QUEUED, ApplicationStatus.APPLIED,
					ApplicationStatus.INTERVIEW));

	public boolean canTransition(ApplicationStatus current, ApplicationStatus requested) {
		return current == requested || ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(requested);
	}

	public void validate(ApplicationStatus current, ApplicationStatus requested) {
		if (!canTransition(current, requested)) {
			throw new BadRequestException("Application status cannot change from " + current + " to " + requested);
		}
	}
}