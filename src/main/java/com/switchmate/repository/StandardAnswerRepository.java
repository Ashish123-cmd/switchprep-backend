package com.switchmate.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.switchmate.domain.entity.StandardAnswer;

public interface StandardAnswerRepository extends JpaRepository<StandardAnswer, UUID> {
}