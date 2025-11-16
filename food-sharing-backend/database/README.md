# 数据库初始化说明

## 文件说明

- `init.sql` - 数据库和表结构初始化脚本
- `test_data.sql` - 测试数据插入脚本

## 使用方法

### 方法一：使用MySQL命令行

1. 打开MySQL命令行客户端或MySQL Workbench

2. 执行初始化脚本：
```bash
mysql -u root -p < init.sql
```

3. 执行测试数据脚本：
```bash
mysql -u root -p < test_data.sql
```

### 方法二：使用MySQL Workbench

1. 打开MySQL Workbench
2. 连接到MySQL服务器
3. 打开 `init.sql` 文件，执行脚本
4. 打开 `test_data.sql` 文件，执行脚本

### 方法三：使用命令行（Windows）

1. 打开命令提示符（CMD）
2. 切换到数据库目录：
```cmd
cd food-sharing-backend\database
```

3. 执行初始化脚本：
```cmd
mysql -u root -p123456 < init.sql
```

4. 执行测试数据脚本：
```cmd
mysql -u root -p123456 < test_data.sql
```

**注意**：请根据你的MySQL密码修改 `-p` 后的密码

## 数据库配置

数据库配置信息（在 `application.yml` 中）：
- 数据库名：`food_sharing`
- 用户名：`root`
- 密码：`123456`（请根据实际情况修改）
- 端口：`3306`
- 字符集：`utf8mb4`

## 测试账号

### 管理员账号
- 用户名：`admin`
- 密码：`password`
- 角色：`ROLE_ADMIN`

### 普通用户账号
- 用户名：`zhangsan`
- 密码：`password`
- 角色：`ROLE_USER`

### 商家账号
- 用户名：`merchant1`
- 密码：`password`
- 角色：`ROLE_MERCHANT`

**注意**：所有测试账号的密码都是明文 `password`（不加密存储）

## 数据结构

### 主要数据表

1. **users** - 用户表
2. **categories** - 分类表
3. **tags** - 标签表
4. **posts** - 帖子表
5. **post_tags** - 帖子标签关联表
6. **comments** - 评论表
7. **likes** - 点赞表
8. **favorites** - 收藏表
9. **follows** - 关注表
10. **merchants** - 商家表
11. **system_configs** - 系统配置表
12. **system_logs** - 系统日志表

## 测试数据说明

### 用户数据
- 1个管理员用户
- 6个普通用户
- 2个商家用户
- 1个禁用用户（用于测试）

### 帖子数据
- 8个已审核通过的帖子
- 2个待审核的帖子
- 1个被拒绝的帖子

### 其他数据
- 20个分类
- 20个标签
- 多条评论、点赞、收藏、关注关系
- 2个商家信息
- 10条系统配置
- 10条系统日志

## 注意事项

1. **密码安全**：测试数据中的密码都是 `password`，生产环境请务必修改
2. **外键约束**：脚本中使用了外键约束，删除数据时请注意
3. **数据备份**：执行脚本前建议备份现有数据
4. **字符集**：确保MySQL支持utf8mb4字符集

## 重置数据库

如果需要重置数据库，可以执行以下SQL：

```sql
DROP DATABASE IF EXISTS `food_sharing`;
```

然后重新执行 `init.sql` 和 `test_data.sql`

