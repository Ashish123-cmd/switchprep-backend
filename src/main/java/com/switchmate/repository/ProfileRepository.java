package com.switchmate.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.switchmate.domain.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
}