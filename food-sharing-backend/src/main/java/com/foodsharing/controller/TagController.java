package com.foodsharing.controller;

import com.foodsharing.dto.TagCreateRequest;
import com.foodsharing.entity.Tag;
import com.foodsharing.repository.TagRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        List<Tag> list = tagRepository.findAll();
        return ResponseEntity.ok(list.stream().map(t -> Map.of("id", t.getId(), "name", t.getName())).toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TagCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "名称不能为空"));
        }
        if (tagRepository.existsByName(req.getName())) {
            return ResponseEntity.badRequest().body(Map.of("message", "标签已存在"));
        }
        Tag t = new Tag();
        t.setName(req.getName().trim());
        tagRepository.save(t);
        return ResponseEntity.ok(Map.of("id", t.getId(), "name", t.getName()));
    }
}
