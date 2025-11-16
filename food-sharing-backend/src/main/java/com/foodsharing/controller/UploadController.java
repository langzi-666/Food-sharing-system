package com.foodsharing.controller;

import com.foodsharing.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final FileStorageService storageService;

    public UploadController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(path = "/image", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadImage(@RequestPart("file") MultipartFile file) {
        try {
            String url = storageService.store(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(path = "/video", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadVideo(@RequestPart("file") MultipartFile file) {
        try {
            String url = storageService.store(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(path = "/images", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadImages(@RequestPart("files") List<MultipartFile> files) {
        try {
            List<String> urls = files.stream().map(f -> {
                try { return storageService.store(f); } catch (Exception e) { throw new RuntimeException(e); }
            }).collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("urls", urls));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
