package com.example.ngoun.controller;

import com.example.ngoun.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final MinioService minioService;

    @PostMapping("/upload/programme")
    public ResponseEntity<Map<String, String>> uploadProgrammeFile(@RequestParam("file") MultipartFile file) {
        String fileName = minioService.uploadFile(file, "programmes");
        String presignedUrl = minioService.getPresignedUrl(fileName, 60);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/media")
    public ResponseEntity<Map<String, String>> uploadMediaFile(@RequestParam("file") MultipartFile file) {
        String fileName = minioService.uploadFile(file, "media");
        String presignedUrl = minioService.getPresignedUrl(fileName, 60);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam(defaultValue = "60") int expiryMinutes) {
        String presignedUrl = minioService.getPresignedUrl(fileName, expiryMinutes);
        
        Map<String, String> response = new HashMap<>();
        response.put("presignedUrl", presignedUrl);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
        minioService.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully");
    }
}
