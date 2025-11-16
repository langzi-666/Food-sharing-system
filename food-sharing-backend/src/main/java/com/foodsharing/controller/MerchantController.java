package com.foodsharing.controller;

import com.foodsharing.dto.MerchantCreateRequest;
import com.foodsharing.dto.MerchantUpdateRequest;
import com.foodsharing.entity.Merchant;
import com.foodsharing.entity.User;
import com.foodsharing.repository.MerchantRepository;
import com.foodsharing.repository.UserRepository;
import com.foodsharing.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public MerchantController(MerchantRepository merchantRepository,
                             UserRepository userRepository,
                             FileStorageService fileStorageService) {
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(Authentication authentication,
                                    @Valid @RequestBody MerchantCreateRequest req) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }

        // 检查是否已经注册过商家
        if (merchantRepository.findByUser(user).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "您已经注册过商家"));
        }

        Merchant merchant = new Merchant();
        merchant.setUser(user);
        merchant.setName(req.getName());
        merchant.setDescription(req.getDescription());
        merchant.setLogo(req.getLogo());
        merchant.setCoverImage(req.getCoverImage());
        merchant.setAddress(req.getAddress());
        merchant.setLatitude(req.getLatitude());
        merchant.setLongitude(req.getLongitude());
        merchant.setPhone(req.getPhone());
        merchant.setBusinessHours(req.getBusinessHours());
        merchant.setAvgPrice(req.getAvgPrice());
        merchant.setCuisineType(req.getCuisineType());
        merchant.setFacilities(req.getFacilities());
        merchant.setLicenseUrl(req.getLicenseUrl());
        merchant.setStatus(2); // 审核中
        merchant.setIsVerified(false);

        Merchant saved = merchantRepository.save(merchant);
        
        // 更新用户角色为商家
        user.setRole("ROLE_MERCHANT");
        userRepository.save(user);

        return ResponseEntity.ok(toMerchantMap(saved));
    }

    @PostMapping(path = "/license", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadLicense(Authentication authentication,
                                          @RequestPart("file") MultipartFile file) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        try {
            String url = fileStorageService.store(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyMerchant(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }

        Merchant merchant = merchantRepository.findByUser(user).orElse(null);
        if (merchant == null) {
            return ResponseEntity.status(404).body(Map.of("message", "未注册商家"));
        }

        return ResponseEntity.ok(toMerchantMap(merchant));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyMerchant(Authentication authentication,
                                            @RequestBody MerchantUpdateRequest req) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("message", "未登录"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "用户不存在"));
        }

        Merchant merchant = merchantRepository.findByUser(user).orElse(null);
        if (merchant == null) {
            return ResponseEntity.status(404).body(Map.of("message", "未注册商家"));
        }

        // 更新信息
        if (req.getName() != null) merchant.setName(req.getName());
        if (req.getDescription() != null) merchant.setDescription(req.getDescription());
        if (req.getLogo() != null) merchant.setLogo(req.getLogo());
        if (req.getCoverImage() != null) merchant.setCoverImage(req.getCoverImage());
        if (req.getAddress() != null) merchant.setAddress(req.getAddress());
        if (req.getLatitude() != null) merchant.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) merchant.setLongitude(req.getLongitude());
        if (req.getPhone() != null) merchant.setPhone(req.getPhone());
        if (req.getBusinessHours() != null) merchant.setBusinessHours(req.getBusinessHours());
        if (req.getAvgPrice() != null) merchant.setAvgPrice(req.getAvgPrice());
        if (req.getCuisineType() != null) merchant.setCuisineType(req.getCuisineType());
        if (req.getFacilities() != null) merchant.setFacilities(req.getFacilities());
        if (req.getStatus() != null) merchant.setStatus(req.getStatus());

        Merchant saved = merchantRepository.save(merchant);
        return ResponseEntity.ok(toMerchantMap(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMerchant(@PathVariable Long id) {
        Merchant merchant = merchantRepository.findById(id).orElse(null);
        if (merchant == null) {
            return ResponseEntity.status(404).body(Map.of("message", "商家不存在"));
        }

        return ResponseEntity.ok(toMerchantMap(merchant));
    }

    @GetMapping
    public ResponseEntity<?> listMerchants(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String q,
                                         @RequestParam(required = false) String cuisineType) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Page<Merchant> result;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating", "reviewCount"));

        if (q != null && !q.isBlank()) {
            result = merchantRepository.searchMerchants(q, pageRequest);
        } else if (cuisineType != null && !cuisineType.isBlank()) {
            result = merchantRepository.findByStatusAndCuisineTypeContaining(1, cuisineType, pageRequest);
        } else {
            result = merchantRepository.findByStatus(1, pageRequest);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("page", result.getNumber());
        resp.put("size", result.getSize());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        resp.put("items", result.getContent().stream().map(this::toMerchantMap).collect(Collectors.toList()));

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<?> getTopRatedMerchants(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Page<Merchant> result = merchantRepository.findTopRatedMerchants(
                PageRequest.of(page, size)
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("page", result.getNumber());
        resp.put("size", result.getSize());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        resp.put("items", result.getContent().stream().map(this::toMerchantMap).collect(Collectors.toList()));

        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> toMerchantMap(Merchant m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        map.put("description", m.getDescription());
        map.put("logo", m.getLogo());
        map.put("coverImage", m.getCoverImage());
        map.put("address", m.getAddress());
        map.put("latitude", m.getLatitude());
        map.put("longitude", m.getLongitude());
        map.put("phone", m.getPhone());
        map.put("businessHours", m.getBusinessHours());
        map.put("avgPrice", m.getAvgPrice());
        map.put("cuisineType", m.getCuisineType());
        map.put("facilities", m.getFacilities());
        map.put("rating", m.getRating());
        map.put("reviewCount", m.getReviewCount());
        map.put("status", m.getStatus());
        map.put("isVerified", m.getIsVerified());
        map.put("createdAt", m.getCreatedAt());
        map.put("updatedAt", m.getUpdatedAt());
        if (m.getUser() != null) {
            map.put("user", Map.of(
                    "id", m.getUser().getId(),
                    "username", m.getUser().getUsername()
            ));
        }
        return map;
    }
}

