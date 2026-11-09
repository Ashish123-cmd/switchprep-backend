package com.switchmate.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.switchmate.domain.entity.Education;

public interface EducationRepository extends JpaRepository<Education, UUID> {

	List<Education> findAllByProfileId(UUID profileId);

	void deleteAllByProfileId(UUID profileId);
}