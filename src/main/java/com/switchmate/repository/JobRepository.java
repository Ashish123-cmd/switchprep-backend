package com.switchmate.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.switchmate.domain.entity.Job;
import com.switchmate.domain.enums.JobSource;

public interface JobRepository extends JpaRepository<Job, UUID> {

	Optional<Job> findBySourceAndExternalId(JobSource source, String externalId);

	List<Job> findAllBySource(JobSource source);

	List<Job> findAllByActiveTrue();
}