# 校园集市 · 本地化部署指南

> ✅ **本项目已在本机完成全部部署与验证**（2026-08-27），环境已全部装好，无需再安装任何软件。
> 日常使用只需按下方「快速启动」双击 3 个脚本即可。

## 快速启动（日常使用）

按顺序双击 `deploy\` 下的三个脚本（每个会开一个黑窗口，**不要关闭**）：

| 顺序 | 脚本 | 作用 | 就绪标志 |
| --- | --- | --- | --- |
| 1 | `start-mysql.bat` | 便携版 MySQL（端口 **3307**，root/root） | 显示 `ready for connections` |
| 2 | `start-backend.bat` | Spring Boot 后端（http://localhost:8080/api） | 显示 `Started CampusMarketApplication` |
| 3 | `start-admin.bat` | 管理后台（http://localhost:5173，admin / admin123） | 显示 `Local: http://localhost:5173/` |
| 4 | `start-app.bat` | **移动端前台** H5 版（http://localhost:5174，淘宝橙界面） | 显示 `Local: http://localhost:5174/` |

移动端也可以直接在 Kimi Work 里点预览卡打开，或用 HBuilderX 打开 `校园集市-uniapp项目骨架\campus-market` 运行到手机（真机需把 `src\api\request.js` 的 BASE_URL 中 `localhost` 改成电脑局域网 IP）。

## 环境说明（已全部装好，无需操作）

| 组件 | 位置 | 说明 |
| --- | --- | --- |
| JDK 17 | `D:\Kimi_Agent_校园交易平台方案\tools\jdk17\jdk-17.0.20.1+1` | 便携版，启动脚本自动配置 JAVA_HOME |
| Maven 3.9.16 | `D:\Kimi_Agent_校园交易平台方案\tools\maven\apache-maven-3.9.16` | 已配阿里云镜像 `D:\tools\maven-settings.xml` |
| MySQL 8.0.29 | `D:\tools\mysql8`（数据在 `D:\tools\mysql8-data`） | 便携版跑在 **3307**，刻意避开你本机已有 MySQL 的 3306；库 `campus_market` 已建，13 张表 + 种子数据已导入 |

> 为什么不用你本机已有的 MySQL？它的 root 密码我们不知道，为避免影响你已有数据，项目独立使用便携版实例（3307 端口）。如需改用自己的 MySQL，编辑后端 `src\main\resources\application.yml` 中的 `spring.datasource` 即可。

## 管理后台功能地图（admin / admin123 登录）

- **数据看板**：今日新增（用户/商品/订单/跑腿单/交易额）、待办提醒、7 日趋势折线图、商品分类饼图、跑腿高峰时段柱状图
- **认证审核**：普通认证 / 跑男申请（跑男通过后自动开通接单权限）
- **用户管理**：搜索、封禁/解封、信用分流水
- **商品管理**：搜索、违规下架（需填原因）
- **跑腿单管理**：全状态浏览，异常单（申诉中/超时）标红
- **投诉处理**：证据预览 + 订单快照 + 仲裁 + 信用分扣减
- **地点管理**：宿舍楼/教学楼/快递点等新增、编辑、启停用
- **公告管理**：发布、预览、下线

## 已验证的端到端链路

- 管理端：登录 → 看板统计实时反映数据 ✓
- 用户端：发验证码（开发期为模拟短信，验证码打印在后端控制台）→ 注册 → 发布商品 ✓
- 免登录浏览：商品列表 / 跑腿大厅已放行，详情与交易仍需登录 ✓

## 常见问题

| 问题 | 排查 |
| --- | --- |
| 后端启动报数据库连接失败 | 先启动 `start-mysql.bat`；确认 3307 端口没被占用 |
| 管理后台列表为空 / 网络异常 | 后端（8080）是否已启动 |
| 注册时不知道验证码 | 看后端黑窗口日志里的「模拟短信」行（6 位数字） |
| 手机真机连不上后端 | 把 uni-app `api/request.js` 的 BASE_URL 中 `localhost` 改为电脑局域网 IP，并放行防火墙 8080 |
| 想彻底重置数据库 | 删掉 `D:\tools\mysql8-data`，重新执行 start-mysql.bat 初始化后，用 `D:\tools\mysql8\bin\mysql.exe -u root -P 3307` 导入根目录的建表脚本 |
