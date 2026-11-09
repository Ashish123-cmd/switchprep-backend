package com.switchmate.storage;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

	String save(UUID userId, MultipartFile file);

	Resource load(String path);
}