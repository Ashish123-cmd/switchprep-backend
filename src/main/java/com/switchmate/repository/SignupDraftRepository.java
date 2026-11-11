package com.switchmate.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.switchmate.domain.entity.SignupDraft;

@Repository
public interface SignupDraftRepository extends JpaRepository<SignupDraft, UUID> {

	List<SignupDraft> findByExpiresAtBefore(Instant now);

	@Query("DELETE FROM SignupDraft s WHERE s.expiresAt < :now")
	void deleteByExpiresAtBefore(@Param("now") Instant now);
}