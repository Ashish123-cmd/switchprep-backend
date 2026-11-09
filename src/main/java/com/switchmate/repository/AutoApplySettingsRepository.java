package com.switchmate.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.switchmate.domain.entity.AutoApplySettings;

public interface AutoApplySettingsRepository extends JpaRepository<AutoApplySettings, UUID> {
}