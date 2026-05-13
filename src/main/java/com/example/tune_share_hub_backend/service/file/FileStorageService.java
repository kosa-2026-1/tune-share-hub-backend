package com.example.tune_share_hub_backend.service.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.example.tune_share_hub_backend.validate.FileValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

	private final Path uploadPath = Path.of("uploads/images");

	public String saveFile(MultipartFile file) {
		FileValidator.validateImageFile(file);

		try {
			Files.createDirectories(uploadPath);

			String originalFilename = file.getOriginalFilename();
			String extension = getExtension(originalFilename);
			String savedFileName = UUID.randomUUID() + extension;

			Path filePath = uploadPath.resolve(savedFileName);

			saveByStream(file, filePath);

			return "/uploads/images/" + savedFileName;

		} catch (IOException e) {
			FileValidator.failFileUpload();
			return null;
		}
	}

	private void saveByStream(MultipartFile file, Path filePath) throws IOException {
		try (
			InputStream inputStream = file.getInputStream();
			OutputStream outputStream = Files.newOutputStream(filePath)
		) {
			byte[] buffer = new byte[1024];
			int bytesRead;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		}
	}

	private String getExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}

		return filename.substring(filename.lastIndexOf("."));
	}
}
