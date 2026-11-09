package com.switchmate.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.switchmate.domain.entity.Application;
import com.switchmate.domain.enums.ApplicationMode;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

	Optional<Application> findByUserIdAndJobId(UUID userId, UUID jobId);

	Optional<Application> findByIdAndUserId(UUID id, UUID userId);

	boolean existsByUserIdAndJobId(UUID userId, UUID jobId);

	long countByUserIdAndModeAndCreatedAtGreaterThanEqual(UUID userId, ApplicationMode mode,
			java.time.Instant createdAt);

	List<Application> findAllByUserId(UUID userId);
}