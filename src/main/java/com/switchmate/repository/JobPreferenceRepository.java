package com.switchmate.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.switchmate.domain.entity.JobPreference;

public interface JobPreferenceRepository extends JpaRepository<JobPreference, UUID> {
}