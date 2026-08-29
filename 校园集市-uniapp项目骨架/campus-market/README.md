# 校园集市 · uni-app 移动端骨架

校内闲置交易 + 跑腿互助平台。技术栈：uni-app（Vue2 语法）+ Vuex，一套代码编译 App / 微信小程序 / H5。

## 目录结构

```
campus-market/
├── pages.json            # 页面路由 + tabBar 配置（21 个页面）
├── manifest.json         # 应用清单（appid 需自行填写）
├── main.js               # 入口，挂载 Vuex 与全局认证拦截
├── App.vue               # 应用生命周期：启动时恢复登录态
├── uni.scss              # 全局样式变量（主色校园绿 #07C160）
├── api/
│   ├── request.js        # 统一请求封装：token / 401 跳登录 / 错误提示（导出 BASE_URL）
│   ├── user.js           # 登录注册、校园认证、信用流水
│   ├── goods.js          # 闲置商品 CRUD、上下架、下单、收藏、我的订单
│   ├── errand.js         # 跑腿全流程接口 + 状态枚举（与后端严格对齐）
│   ├── common.js         # 分类 / 地点库 / 公告 / 图片上传（uni.uploadFile 封装）
│   ├── message.js        # 聊天会话、聊天记录、发送、已读、未读数
│   ├── complaint.js      # 投诉提交与查询 + 投诉类型枚举
│   └── evaluate.js       # 评价提交、用户评价列表
├── store/index.js        # Vuex：登录态、认证状态、跑男资格
├── utils/index.js        # checkAuth 认证拦截、timeAgo、防连点锁
├── static/               # 静态资源（默认头像等）
└── pages/                # 21 个页面，详见 pages.json
```

## 页面清单

| 分组 | 页面 | 说明 |
|---|---|---|
| TabBar | pages/index/index | 首页：分类 + 双列商品流 + 搜索入口 |
| TabBar | pages/errand/hall | 跑腿大厅：类型筛选 + 排序 + 抢单（防连点） |
| TabBar | pages/publish/index | 发布入口：发闲置 / 发跑腿 二选一 |
| TabBar | pages/message/list | 消息：聊天会话 + 系统通知 |
| TabBar | pages/user/center | 个人中心：认证状态、信用分、功能入口 |
| 闲置 | pages/goods/detail | 商品详情：轮播、卖家卡、收藏/聊天/下单 |
| 闲置 | pages/goods/publish | 发布闲置：图片上传、分类/地点选择（接口拉取） |
| 闲置 | pages/goods/search | 搜索：历史记录（本地）+ 结果流 |
| 闲置 | pages/goods/my | 我的发布：状态筛选 + 上下架 |
| 闲置 | pages/order/list | 我的订单：买到/卖出，确认完成/取消/评价/投诉 |
| 跑腿 | pages/errand/publish | 发布跑腿：地点选择器 + 期望时间快捷选项 |
| 跑腿 | pages/errand/detail | 跑腿单详情：状态时间线 + 按钮矩阵（状态×角色） |
| 跑腿 | pages/errand/my | 我的跑腿：我发的 / 我接的 + 状态筛选 |
| 用户 | pages/user/login / register / auth | 登录、注册、校园认证（含跑男认证入口） |
| 用户 | pages/user/credit | 信用分明细：当前分 + 变动流水 |
| 用户 | pages/user/favorite | 我的收藏：列表 + 取消收藏 |
| 售后 | pages/evaluate/index | 评价页：星级 + 标签 + 文字 |
| 售后 | pages/complaint/index | 投诉页：类型 + 描述 + 证据图上传 |
| 消息 | pages/message/chat | 聊天页：气泡对话 + 3s 轮询（可升级 WebSocket） |

## 运行方式（HBuilderX 推荐）

1. 用 HBuilderX 打开本目录（文件 → 打开目录）
2. 在 manifest.json 中填入自己的微信小程序 appid（测试号即可）
3. 运行 → 运行到浏览器 / 微信开发者工具 / 手机模拟器

## 与后端对接

- 后端基地址：`api/request.js` 顶部 `BASE_URL`，默认 `http://localhost:8080/api`
- 接口契约：见《校园集市-跑腿状态机与接口设计.md》第 2 节与第 5 节补充接口
- 数据库建表：执行项目根目录《校园集市-数据库建表脚本.sql》（MySQL 8.0+）

## 降级策略（骨架阶段后端未启动也能跑）

- 分类 / 地点 / 会话 / 公告拉取失败时静默降级，页面不白屏
- 图片上传失败时降级为本地临时路径，保证发布流程可演示
- 聊天页轮询失败静默重试，不反复弹错

## 已完成 / 待补

已完成：21 个页面全部可路由跳转；API 层 8 个模块；认证拦截；跑腿按钮矩阵（状态×角色）；
分类/地点选择器；图片上传封装；聊天轮询；评价/投诉/收藏/信用流水页面。

待补（不影响主流程演示）：
- 聊天 WebSocket 实时推送（当前为 3 秒轮询）
- 卖家主页（展示在售商品 + 收到的评价）
- 管理后台 Web 端（页面清单见原型文档第 4 节）
- tabBar 中央凸出发布按钮（App 端可用 midButton 配置）
