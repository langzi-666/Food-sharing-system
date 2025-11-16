@echo off
chcp 65001 >nul
title Food Sharing System - 快捷启动

cd food-sharing-backend
start "后端服务" cmd /k "mvn spring-boot:run"
cd ..
cd food-sharing-frontend
start "前端服务" cmd /k "npm run dev"
cd ..