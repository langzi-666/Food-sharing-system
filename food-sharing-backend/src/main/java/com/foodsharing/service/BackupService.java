package com.foodsharing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据备份服务
 */
@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    @Value("${backup.directory:backups}")
    private String backupDirectory;

    @Value("${backup.enabled:true}")
    private boolean backupEnabled;

    /**
     * 创建备份目录
     */
    private void ensureBackupDirectory() {
        try {
            Path path = Paths.get(backupDirectory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created backup directory: {}", backupDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to create backup directory", e);
        }
    }

    /**
     * 创建数据库备份脚本（SQL格式）
     * 注意：实际生产环境应使用mysqldump等工具
     */
    @Async
    public void createBackup(String backupType) {
        if (!backupEnabled) {
            logger.info("Backup is disabled");
            return;
        }

        ensureBackupDirectory();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_%s.sql", backupType, timestamp);
        String filePath = Paths.get(backupDirectory, fileName).toString();

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("-- Database Backup\n");
            writer.write("-- Type: " + backupType + "\n");
            writer.write("-- Created: " + LocalDateTime.now() + "\n");
            writer.write("-- Note: This is a metadata backup. Use mysqldump for full database backup.\n\n");

            // 这里可以添加实际的备份逻辑
            // 例如：导出配置、导出关键数据等
            writer.write("-- Backup completed successfully\n");

            logger.info("Backup created successfully: {}", filePath);
        } catch (IOException e) {
            logger.error("Failed to create backup", e);
        }
    }

    /**
     * 获取备份文件列表
     */
    public List<String> listBackups() {
        List<String> backups = new ArrayList<>();
        try {
            Path path = Paths.get(backupDirectory);
            if (Files.exists(path)) {
                Files.list(path)
                        .filter(p -> p.toString().endsWith(".sql"))
                        .map(p -> p.getFileName().toString())
                        .sorted()
                        .forEach(backups::add);
            }
        } catch (IOException e) {
            logger.error("Failed to list backups", e);
        }
        return backups;
    }

    /**
     * 删除旧备份（保留最近N个）
     */
    @Async
    public void cleanupOldBackups(int keepCount) {
        try {
            Path path = Paths.get(backupDirectory);
            if (Files.exists(path)) {
                List<Path> backups = Files.list(path)
                        .filter(p -> p.toString().endsWith(".sql"))
                        .sorted((a, b) -> {
                            try {
                                return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
                            } catch (IOException e) {
                                return 0;
                            }
                        })
                        .toList();

                if (backups.size() > keepCount) {
                    for (int i = keepCount; i < backups.size(); i++) {
                        Files.delete(backups.get(i));
                        logger.info("Deleted old backup: {}", backups.get(i).getFileName());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to cleanup old backups", e);
        }
    }

    /**
     * 手动触发备份
     */
    public String manualBackup() {
        createBackup("manual");
        return "Backup initiated";
    }
}

