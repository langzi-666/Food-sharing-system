# 美食分享系统MySQL数据库设计文档

## 1. 数据库概述

### 1.1 数据库信息
- **数据库名称**: food_sharing
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **MySQL版本**: 8.0+

### 1.2 设计原则
- 遵循第三范式
- 合理使用索引
- 预留扩展字段
- 统一命名规范

## 2. 数据表设计

### 2.1 用户表 (users)
```sql
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(加密)',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint DEFAULT '0' COMMENT '性别:0-未知,1-男,2-女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `bio` text COMMENT '个人简介',
  `location` varchar(100) DEFAULT NULL COMMENT '所在地',
  `user_type` tinyint DEFAULT '1' COMMENT '用户类型:1-普通用户,2-认证用户,3-商家',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `level` int DEFAULT '1' COMMENT '用户等级',
  `points` int DEFAULT '0' COMMENT '积分',
  `follower_count` int DEFAULT '0' COMMENT '粉丝数',
  `following_count` int DEFAULT '0' COMMENT '关注数',
  `post_count` int DEFAULT '0' COMMENT '发帖数',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 2.2 美食内容表 (foods)
```sql
CREATE TABLE `foods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '内容ID',
  `user_id` bigint NOT NULL COMMENT '发布用户ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容描述',
  `images` json DEFAULT NULL COMMENT '图片URLs(JSON数组)',
  `video_url` varchar(255) DEFAULT NULL COMMENT '视频URL',
  `location` varchar(100) DEFAULT NULL COMMENT '地理位置',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `price_range` varchar(50) DEFAULT NULL COMMENT '价格区间',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签(逗号分隔)',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  `cuisine_type` varchar(50) DEFAULT NULL COMMENT '菜系类型',
  `difficulty` tinyint DEFAULT NULL COMMENT '制作难度:1-简单,2-中等,3-困难',
  `cooking_time` int DEFAULT NULL COMMENT '制作时间(分钟)',
  `serving_size` int DEFAULT NULL COMMENT '份量(人数)',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `comment_count` int DEFAULT '0' COMMENT '评论数',
  `share_count` int DEFAULT '0' COMMENT '分享数',
  `collect_count` int DEFAULT '0' COMMENT '收藏数',
  `view_count` int DEFAULT '0' COMMENT '浏览数',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-草稿,1-已发布,2-已删除',
  `is_featured` tinyint DEFAULT '0' COMMENT '是否精选:0-否,1-是',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_like_count` (`like_count`),
  KEY `idx_location` (`location`),
  CONSTRAINT `fk_foods_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美食内容表';
```

### 2.3 商家表 (merchants)
```sql
CREATE TABLE `merchants` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `name` varchar(100) NOT NULL COMMENT '商家名称',
  `description` text COMMENT '商家描述',
  `logo` varchar(255) DEFAULT NULL COMMENT '商家logo',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `business_hours` varchar(200) DEFAULT NULL COMMENT '营业时间',
  `avg_price` decimal(8,2) DEFAULT NULL COMMENT '人均消费',
  `cuisine_type` varchar(100) DEFAULT NULL COMMENT '菜系类型',
  `facilities` varchar(500) DEFAULT NULL COMMENT '设施服务',
  `rating` decimal(3,2) DEFAULT '0.00' COMMENT '评分',
  `review_count` int DEFAULT '0' COMMENT '评价数量',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-停业,1-营业,2-审核中',
  `is_verified` tinyint DEFAULT '0' COMMENT '是否认证:0-未认证,1-已认证',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_name` (`name`),
  KEY `idx_status` (`status`),
  KEY `idx_rating` (`rating`),
  CONSTRAINT `fk_merchants_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家表';
```

### 2.4 分类表 (categories)
```sql
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标',
  `parent_id` int DEFAULT '0' COMMENT '父分类ID',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-禁用,1-启用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';
```

### 2.5 评论表 (comments)
```sql
CREATE TABLE `comments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `food_id` bigint NOT NULL COMMENT '美食内容ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父评论ID',
  `content` text NOT NULL COMMENT '评论内容',
  `images` json DEFAULT NULL COMMENT '评论图片',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `reply_count` int DEFAULT '0' COMMENT '回复数',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-删除,1-正常',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_food_id` (`food_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_created_time` (`created_time`),
  CONSTRAINT `fk_comments_food_id` FOREIGN KEY (`food_id`) REFERENCES `foods` (`id`),
  CONSTRAINT `fk_comments_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

### 2.6 点赞表 (likes)
```sql
CREATE TABLE `likes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `target_type` tinyint NOT NULL COMMENT '目标类型:1-美食,2-评论',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_id`,`target_type`),
  KEY `idx_target` (`target_id`,`target_type`),
  CONSTRAINT `fk_likes_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';
```

### 2.7 收藏表 (collections)
```sql
CREATE TABLE `collections` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `food_id` bigint NOT NULL COMMENT '美食内容ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_food` (`user_id`,`food_id`),
  KEY `idx_food_id` (`food_id`),
  CONSTRAINT `fk_collections_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_collections_food_id` FOREIGN KEY (`food_id`) REFERENCES `foods` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
```

### 2.8 关注表 (follows)
```sql
CREATE TABLE `follows` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关注ID',
  `follower_id` bigint NOT NULL COMMENT '关注者ID',
  `following_id` bigint NOT NULL COMMENT '被关注者ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`,`following_id`),
  KEY `idx_following_id` (`following_id`),
  CONSTRAINT `fk_follows_follower_id` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_follows_following_id` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';
```

### 2.9 标签表 (tags)
```sql
CREATE TABLE `tags` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `color` varchar(20) DEFAULT NULL COMMENT '标签颜色',
  `use_count` int DEFAULT '0' COMMENT '使用次数',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-禁用,1-启用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_use_count` (`use_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';
```

### 2.10 系统配置表 (system_config)
```sql
CREATE TABLE `system_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `description` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

## 3. 索引设计

### 3.1 主要索引说明
- **主键索引**: 所有表都有自增主键
- **唯一索引**: 用户名、邮箱、手机号等唯一字段
- **外键索引**: 关联字段建立索引
- **业务索引**: 根据查询需求建立的复合索引

### 3.2 性能优化索引
```sql
-- 美食内容查询优化
CREATE INDEX idx_foods_user_status_time ON foods(user_id, status, created_time);
CREATE INDEX idx_foods_category_status_like ON foods(category_id, status, like_count);

-- 评论查询优化
CREATE INDEX idx_comments_food_status_time ON comments(food_id, status, created_time);

-- 关注关系查询优化
CREATE INDEX idx_follows_follower_time ON follows(follower_id, created_time);
```

## 4. 数据初始化

### 4.1 分类数据初始化
```sql
INSERT INTO `categories` (`name`, `description`, `parent_id`, `sort_order`) VALUES
('中式菜系', '中国传统菜系', 0, 1),
('川菜', '四川菜系', 1, 1),
('粤菜', '广东菜系', 1, 2),
('鲁菜', '山东菜系', 1, 3),
('苏菜', '江苏菜系', 1, 4),
('西式菜系', '西方菜系', 0, 2),
('意大利菜', '意大利菜系', 6, 1),
('法国菜', '法国菜系', 6, 2),
('甜品饮品', '甜品和饮品', 0, 3),
('蛋糕', '各式蛋糕', 9, 1),
('饮品', '各种饮品', 9, 2);
```

### 4.2 系统配置初始化
```sql
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('site_name', '美食分享系统', '网站名称'),
('upload_max_size', '5242880', '上传文件最大大小(字节)'),
('image_formats', 'jpg,jpeg,png,gif', '支持的图片格式'),
('video_formats', 'mp4,avi', '支持的视频格式'),
('default_avatar', '/images/default-avatar.png', '默认头像'),
('points_per_post', '10', '发帖获得积分'),
('points_per_like', '1', '点赞获得积分');
```

## 5. 数据库维护

### 5.1 备份策略
- 每日全量备份
- 每小时增量备份
- 保留30天备份数据

### 5.2 监控指标
- 连接数监控
- 慢查询监控
- 磁盘空间监控
- 性能指标监控

### 5.3 优化建议
- 定期分析表结构
- 优化慢查询SQL
- 合理使用分区表
- 定期清理无用数据

---

**文档版本**: v1.0  
**创建日期**: 2024年11月15日  
**数据库版本**: MySQL 8.0+
