#!/bin/bash

# 美食分享系统数据库初始化脚本

echo "============================================"
echo "美食分享系统数据库初始化脚本"
echo "============================================"
echo ""

MYSQL_USER="root"
MYSQL_PASSWORD="123456"
MYSQL_DATABASE="food_sharing"

echo "正在初始化数据库..."
echo ""

echo "[1/2] 创建数据库和表结构..."
mysql -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" < init.sql
if [ $? -ne 0 ]; then
    echo "错误：数据库初始化失败！"
    exit 1
fi
echo "数据库和表结构创建成功！"
echo ""

echo "[2/2] 插入测试数据..."
mysql -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" < test_data.sql
if [ $? -ne 0 ]; then
    echo "错误：测试数据插入失败！"
    exit 1
fi
echo "测试数据插入成功！"
echo ""

echo "============================================"
echo "数据库初始化完成！"
echo "============================================"
echo ""
echo "测试账号："
echo "  管理员：admin / password"
echo "  普通用户：zhangsan / password"
echo "  商家：merchant1 / password"
echo ""

