package com.example.file_upload.repository;

import com.example.file_upload.entity.FileEntity;
import com.example.file_upload.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByOwner(User owner);
    Optional<FileEntity> findByTinyUrl(String tinyUrl);
}
