package com.switchmate.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.switchmate.common.exception.BadRequestException;

@Component
public class LocalDiskFileStorage implements FileStorage {

	private final Path rootDirectory;

	public LocalDiskFileStorage(@Value("${app.storage.resume-directory}") String directory) {
		this.rootDirectory = Path.of(directory).toAbsolutePath().normalize();
	}

	@Override
	public String save(UUID userId, MultipartFile file) {
		String contentType = file.getContentType() == null ? "" : file.getContentType().split(";", 2)[0].strip();
		String extension = switch (contentType) {
			case "application/pdf" -> ".pdf";
			case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
			default -> throw new BadRequestException("Resume must be a PDF or DOCX file");
		};
		String generatedName = userId + "-" + UUID.randomUUID() + extension;
		Path destination = rootDirectory.resolve(generatedName).normalize();
		if (!destination.startsWith(rootDirectory)) {
			throw new IllegalStateException("Generated storage path is invalid");
		}
		try {
			Files.createDirectories(rootDirectory);
			try (var input = file.getInputStream()) {
				Files.copy(input, destination);
			}
			return generatedName;
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not store resume file", exception);
		}
	}

	@Override
	public Resource load(String path) {
		Path resolved = rootDirectory.resolve(path).normalize();
		if (!resolved.startsWith(rootDirectory)) {
			throw new IllegalArgumentException("Invalid storage path");
		}
		return new FileSystemResource(resolved);
	}
}