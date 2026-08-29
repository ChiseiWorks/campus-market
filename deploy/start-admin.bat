@echo off
chcp 65001 >nul
title 校园集市 - 管理后台 (Vue3 + Vite)
echo ============================================
echo   校园集市 管理后台启动中...
echo   访问地址: http://localhost:5173
echo   默认账号: admin / admin123
echo   （依赖后端 http://localhost:8080 已启动）
echo ============================================
set "PATH=C:\Users\lenovo\AppData\Local\Programs\kimi-desktop\resources\resources\runtime;%PATH%"
cd /d "%~dp0..\校园集市-admin管理后台\admin-web"
if not exist node_modules (
  echo 首次运行，正在安装依赖...
  npm install
)
npm run dev
pause
