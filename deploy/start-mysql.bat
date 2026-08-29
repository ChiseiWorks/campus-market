@echo off
chcp 65001 >nul
title 校园集市 - MySQL 数据库 (便携版, 端口 3307)
echo ============================================
echo   便携版 MySQL 8.0 启动中...
echo   端口: 3307（避开你本机已有 MySQL 的 3306）
echo   账号: root / root
echo   数据库: campus_market
echo ============================================
"D:\tools\mysql8\bin\mysqld.exe" --datadir="D:\tools\mysql8-data" --port=3307 --console
pause
