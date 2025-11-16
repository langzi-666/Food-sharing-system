package com.foodsharing.controller;

import com.foodsharing.dto.CategoryCreateRequest;
import com.foodsharing.entity.Category;
import com.foodsharing.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        List<Category> list = categoryRepository.findAll();
        return ResponseEntity.ok(list.stream().map(c -> Map.of("id", c.getId(), "name", c.getName())).toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CategoryCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "名称不能为空"));
        }
        if (categoryRepository.existsByName(req.getName())) {
            return ResponseEntity.badRequest().body(Map.of("message", "分类已存在"));
        }
        Category c = new Category();
        c.setName(req.getName().trim());
        categoryRepository.save(c);
        return ResponseEntity.ok(Map.of("id", c.getId(), "name", c.getName()));
    }
}
