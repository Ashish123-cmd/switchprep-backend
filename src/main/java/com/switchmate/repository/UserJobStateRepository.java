package com.switchmate.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.switchmate.domain.entity.UserJobState;
import com.switchmate.domain.entity.UserJobStateId;

public interface UserJobStateRepository extends JpaRepository<UserJobState, UserJobStateId> {

	List<UserJobState> findAllByIdUserId(UUID userId);
}