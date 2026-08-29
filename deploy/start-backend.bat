@echo off
chcp 65001 >nul
title 校园集市 - 后端服务 (Spring Boot)
echo ============================================
echo   校园集市 后端服务启动中...
echo   接口地址: http://localhost:8080/api
echo   首次启动会下载 Maven 依赖，请耐心等待
echo   （请先启动 deploy\start-mysql.bat）
echo ============================================
set "JAVA_HOME=D:\Kimi_Agent_校园交易平台方案\tools\jdk17\jdk-17.0.20.1+1"
set "PATH=%JAVA_HOME%\bin;D:\Kimi_Agent_校园交易平台方案\tools\maven\apache-maven-3.9.16\bin;%PATH%"
cd /d "%~dp0..\校园集市-springboot后端\campus-market-server"
mvn -s "D:\tools\maven-settings.xml" spring-boot:run
pause
