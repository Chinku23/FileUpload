package com.example.file_upload.service;

import com.example.file_upload.config.JwtUtil;
import com.example.file_upload.entity.FileEntity;
import com.example.file_upload.entity.User;
import com.example.file_upload.repository.FileRepository;
import com.example.file_upload.repository.UserRepository;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final String uploadDir = "uploads/";

    public FileService(FileRepository fileRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public FileEntity uploadFile(String token, MultipartFile file, String title, String description, boolean compress) throws IOException {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String fileType = FilenameUtils.getExtension(file.getOriginalFilename());
        long fileSize = file.getSize();
        String storagePath = uploadDir + fileName;

        // Save file to disk
        Files.createDirectories(Paths.get(uploadDir));
        Files.write(Paths.get(storagePath), file.getBytes());

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileName);
        fileEntity.setFileType(fileType);
        fileEntity.setFileSize(fileSize);
        fileEntity.setStoragePath(storagePath);
        fileEntity.setTitle(title);
        fileEntity.setDescription(description);
        fileEntity.setCompressed(compress);
        fileEntity.setOwner(user);

        return fileRepository.save(fileEntity);
    }

    public List<FileEntity> getUserFiles(String token) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return fileRepository.findByOwner(user);
    }

    public void deleteFile(String token, Long fileId) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!fileEntity.getOwner().equals(user)) {
            throw new RuntimeException("Unauthorized");
        }

        // Delete file from disk
        try {
            Files.deleteIfExists(Paths.get(fileEntity.getStoragePath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file");
        }

        fileRepository.delete(fileEntity);
    }

    public String generateTinyUrl(Long fileId, String token) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        FileEntity fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!fileEntity.getOwner().equals(user)) {
            throw new RuntimeException("Unauthorized");
        }

        String tinyUrl = generateRandomString();
        fileEntity.setTinyUrl(tinyUrl);
        fileRepository.save(fileEntity);

        return "http://localhost:8989/files/public/" + tinyUrl;
    }

    public byte[] downloadFile(String tinyUrl) throws IOException {
        FileEntity fileEntity = fileRepository.findByTinyUrl(tinyUrl)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return Files.readAllBytes(Paths.get(fileEntity.getStoragePath()));
    }

    private String generateRandomString() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
