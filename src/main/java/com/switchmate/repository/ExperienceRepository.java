package com.switchmate.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.switchmate.domain.entity.Experience;

public interface ExperienceRepository extends JpaRepository<Experience, UUID> {

	List<Experience> findAllByProfileId(UUID profileId);

	void deleteAllByProfileId(UUID profileId);
}