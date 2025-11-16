package com.foodsharing.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads";
    private static final String THUMBNAIL_DIR = "uploads/thumbnails";
    private static final int THUMBNAIL_MAX_WIDTH = 300;
    private static final int THUMBNAIL_MAX_HEIGHT = 300;

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("空文件");
        }
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = datePrefix + "-" + UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        
        // 如果是图片，异步生成缩略图
        if (isImageFile(ext)) {
            generateThumbnailAsync(target, filename);
        }
        
        return "/uploads/" + filename;
    }

    private boolean isImageFile(String ext) {
        if (ext == null) return false;
        String lowerExt = ext.toLowerCase();
        return lowerExt.equals(".jpg") || lowerExt.equals(".jpeg") || 
               lowerExt.equals(".png") || lowerExt.equals(".gif") || 
               lowerExt.equals(".bmp") || lowerExt.equals(".webp");
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> generateThumbnailAsync(Path originalPath, String filename) {
        try {
            generateThumbnail(originalPath, filename);
        } catch (IOException e) {
            // 缩略图生成失败不影响主流程
            System.err.println("生成缩略图失败: " + e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    private void generateThumbnail(Path originalPath, String filename) throws IOException {
        Path thumbnailPath = Paths.get(THUMBNAIL_DIR);
        if (!Files.exists(thumbnailPath)) {
            Files.createDirectories(thumbnailPath);
        }

        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        if (originalImage == null) {
            return;
        }

        // 计算缩略图尺寸
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        double scale = Math.min(
            (double) THUMBNAIL_MAX_WIDTH / originalWidth,
            (double) THUMBNAIL_MAX_HEIGHT / originalHeight
        );
        
        int thumbnailWidth = (int) (originalWidth * scale);
        int thumbnailHeight = (int) (originalHeight * scale);

        // 创建缩略图
        BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumbnail.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
        g.dispose();

        // 保存缩略图
        String ext = filename.substring(filename.lastIndexOf('.'));
        String thumbnailFilename = "thumb_" + filename;
        Path thumbnailFile = thumbnailPath.resolve(thumbnailFilename);
        ImageIO.write(thumbnail, ext.substring(1), thumbnailFile.toFile());
    }

    public String getThumbnailUrl(String originalUrl) {
        if (originalUrl == null || !originalUrl.startsWith("/uploads/")) {
            return originalUrl;
        }
        String filename = originalUrl.substring(originalUrl.lastIndexOf('/') + 1);
        return "/uploads/thumbnails/thumb_" + filename;
    }
}
