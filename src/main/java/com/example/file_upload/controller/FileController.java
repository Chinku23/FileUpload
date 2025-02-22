package com.example.file_upload.controller;

import com.example.file_upload.entity.FileEntity;
import com.example.file_upload.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileEntity> uploadFile(
            @RequestHeader("Token") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "compress", defaultValue = "false") boolean compress) throws IOException {

        FileEntity uploadedFile = fileService.uploadFile(token, file, title, description, compress);
        return ResponseEntity.ok(uploadedFile);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileEntity>> listFiles(@RequestHeader("Token") String token) {
        return ResponseEntity.ok(fileService.getUserFiles(token));
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<String> deleteFile(@RequestHeader("Token") String token, @PathVariable Long fileId) {
        fileService.deleteFile(token, fileId);
        return ResponseEntity.ok("File deleted successfully");
    }

    @PostMapping("/share/{fileId}")
    public ResponseEntity<String> generateTinyUrl(@RequestHeader("Token") String token, @PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.generateTinyUrl(fileId, token));
    }

    @GetMapping("/public/{tinyUrl}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String tinyUrl) throws IOException {
        return ResponseEntity.ok(fileService.downloadFile(tinyUrl));
    }
}
