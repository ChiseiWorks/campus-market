@echo off
chcp 65001 >nul
title 校园集市 - 移动端前台 (uni-app H5)
echo ============================================
echo   校园集市 移动端前台启动中...
echo   访问地址: http://localhost:5174
echo   （商品/跑腿数据依赖后端 8080 已启动）
echo ============================================
set "PATH=C:\Users\lenovo\AppData\Local\Programs\kimi-desktop\resources\resources\runtime;%PATH%"
cd /d "%~dp0..\校园集市-uniapp项目骨架\campus-market"
if not exist node_modules (
  echo 首次运行，正在安装依赖...
  npm install --registry=https://registry.npmmirror.com
)
npm run dev -- --port 5174
pause
