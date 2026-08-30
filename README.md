# 校园集市 · 校园交易平台
在大学这个小社会当中每当学长毕业拿不走的东西会当废品一样丢弃，这样给保洁阿姨极大的增加了工作负担并且极大的浪费了资源还会污染环境，这个项目的初衷就是在学校内搭建起一个二手交易平台比，其功能不但有二手产品的交易还有一些悬赏任务比如代课代校园跑等一些日常需求
面向校园场景的二手交易 + 跑腿互助平台，包含 **用户端（uni-app / H5）**、**管理后台（Vue3）**、**后端（Spring Boot 3）** 三部分。

## 项目结构

| 目录 | 说明 |
|---|---|
| `校园集市-springboot后端/campus-market-server` | Spring Boot 3.2 + MyBatis-Plus + JWT + MySQL 后端 |
| `校园集市-admin管理后台/admin-web` | Vue3 + Vite + Element Plus + ECharts 管理后台 |
| `校园集市-uniapp项目骨架/campus-market` | uni-app（Vue3 + Vite）用户端，支持 H5 / 小程序 |
| `deploy/` | 本地一键部署脚本（Windows bat）与部署说明 |
| `校园集市-数据库建表脚本.sql` | MySQL 建表脚本（13 张表，含演示数据） |
| `*.md` | 数据库设计、页面原型、跑腿状态机等设计文档 |

## 核心功能

- **用户端**：注册登录（短信验证码模拟）、发布/浏览二手商品、下单、跑腿任务发布与抢单（乐观锁防超抢）、信用分体系、消息通知、收货地点管理
- **管理后台**：数据看板（ECharts）、用户审核与封禁、商品/订单/跑腿管理、地点管理、信用流水、管理员登录（JWT 双拦截器，用户/管理端隔离）

## 本地运行

详见 `deploy/README.md`。简要：

1. 导入 `校园集市-数据库建表脚本.sql` 到 MySQL（库名 `campus_market`）
2. 后端：`mvn spring-boot:run`（端口 8080）
3. 管理后台：`npm install && npm run dev`
4. 用户端 H5：`npm install && npm run dev:h5`

## 配置说明

`application.yml` 中数据库密码、JWT 密钥、管理员账号均支持环境变量覆盖（`DB_PASSWORD` / `JWT_SECRET` / `ADMIN_USERNAME` / `ADMIN_PASSWORD`），默认值仅供本地开发，**生产部署务必通过环境变量覆盖**。

默认管理后台账号：`admin / admin123`（本地开发用）。

## 技术栈

Spring Boot 3.2.5 · MyBatis-Plus · MySQL 8 · JWT(HS256) · BCrypt · Vue 3 · Vite · Element Plus · ECharts · uni-app(Vue3)
