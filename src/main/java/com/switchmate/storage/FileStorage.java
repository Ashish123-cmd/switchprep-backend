package com.switchmate.storage;

import java.nio.file.Path;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

	String save(UUID userId, MultipartFile file);

	Path saveDraft(String generatedName, MultipartFile file);

	String moveDraft(String draftFileName, UUID userId);

	Resource load(String path);
}