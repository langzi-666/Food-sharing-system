package com.foodsharing.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class PostCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String imageUrl;
    private String videoUrl;
    private Long categoryId;
    private List<Long> tagIds;
    private Double lat;
    private Double lng;
    private String address;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
