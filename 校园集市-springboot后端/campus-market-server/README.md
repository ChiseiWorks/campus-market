# 校园集市 · Spring Boot 后端

校内闲置交易 + 跑腿互助平台后端骨架。与设计文档严格对齐：

- 《校园集市-数据库设计文档.md》—— 13 张表 / 字段 / 索引 / 设计决策
- 《校园集市-跑腿状态机与接口设计.md》—— 状态机 / 30+ 接口 / 核心代码（已照抄落地）
- 《校园集市-数据库建表脚本.sql》—— 可执行 DDL（实体类字段以此为准）

## 技术栈

- Spring Boot 3.2.5 / Java 17 / Maven
- MyBatis-Plus 3.5.7（分页插件 / 逻辑删除 / 自动填充 / 自定义 CAS SQL）
- MySQL 8.0（库名 `campus_market`）
- JWT（jjwt 0.11.5）+ AuthInterceptor 登录拦截
- spring-security-crypto（只用 BCrypt，未引入完整 Spring Security）

## 目录结构

```
campus-market-server
├── pom.xml
├── src/main
│   ├── resources
│   │   └── application.yml          # 数据源 / JWT / 上传目录配置
│   └── java/com/campus/market
│       ├── CampusMarketApplication.java
│       ├── common/                  # Result 统一响应体 / BizException / PageResult
│       │                            # GlobalExceptionHandler / JwtUtil / OrderNoUtil
│       ├── config/                  # WebMvcConfig(拦截器+静态资源) / MybatisPlusConfig
│       │                            # MyMetaObjectHandler(自动填充) / CommonConfig(BCrypt)
│       ├── enums/                   # ErrandStatusEnum（7 状态 + 显式流转表）
│       ├── interceptor/             # AuthInterceptor（Bearer Token → userId）
│       ├── entity/                  # 13 个实体类（严格对照建表脚本）
│       ├── mapper/                  # 13 个 Mapper（含抢单/乐观锁/信用分 CAS SQL）
│       ├── dto/                     # 12 个请求 DTO（带 jakarta validation 校验）
│       ├── service/                 # 9 个 Service 接口 + ErrandStateMachine
│       │   └── impl/                # 9 个 Service 实现
│       ├── controller/              # 8 个 Controller（路径 = 接口文档 /api 前缀）
│       └── admin/                   # 管理端：controller(7) / service(7+7) / dto(7)
```

## 快速开始

### 1. 建库建表

```bash
mysql -u root -p < ../校园集市-数据库建表脚本.sql
```

脚本会创建 `campus_market` 库、13 张表和初始数据（分类 / 地点 / 公告）。

### 2. 修改配置

编辑 `src/main/resources/application.yml`：

- `spring.datasource.username / password` → 本机 MySQL 账号密码
- `jwt.secret` → 生产环境务必更换（至少 32 字节）
- `file.upload-dir` → 上传文件存储目录（默认项目运行目录下 `uploads/`）

### 3. 启动

```bash
mvn spring-boot:run
# 或
mvn package && java -jar target/campus-market-server-1.0.0.jar
```

服务端口 `8080`，无 context-path，接口完整路径自带 `/api` 前缀，
与前端 `request.js` 的 `BASE_URL = http://localhost:8080/api` 一致。

上传的图片通过 `http://localhost:8080/uploads/{文件名}` 访问。

## 接口清单（索引到设计文档）

统一约定：`{ code: 200, msg, data }`；非 200 即业务错误；未登录返回 HTTP 401（前端自动跳登录）。

| 模块 | 路径前缀 | 接口 | 文档位置 |
|---|---|---|---|
| 用户 | `/api/user` | login / register / sms / info / auth / credit/logs | 接口文档 5.1 |
| 闲置商品 | `/api/goods` | list / {id} / publish / my / offshelf / onshelf / favorite / favorite/my | 接口文档 5.2 |
| 闲置订单 | `/api/goods/order` | create / my / {id}/finish / {id}/cancel | 接口文档 5.2 |
| 跑腿单 | `/api/errand` | publish / hall / {id} / accept / deliver / arrive / confirm / cancel / dispute / my/publish / my/accept | 接口文档第 2 节 |
| 基础数据 | `/api` | category/list / location/list / notice/list / file/upload | 接口文档 5.3 |
| 消息 | `/api/message` | sessions / history / send / read / unread/count | 接口文档 5.4 |
| 评价 | `/api/evaluate` | submit / user/{userId} / my | 接口文档 5.5 |
| 投诉 | `/api/complaint` | submit / my / {id} | 接口文档 5.5 |

管理端接口（`/api/admin/**`，JWT 需带 role=admin，由 AdminInterceptor 校验，`/api/admin/login` 放行）：

| 模块 | 路径前缀 | 接口 |
|---|---|---|
| 管理员登录 | `/api/admin` | POST login {username,password} → {token, adminInfo}（账号配置在 application.yml `app.admin.*`，不走数据库） |
| 认证审核 | `/api/admin/auth` | GET list?status&page&size ・ POST approve {id} ・ POST reject {id,remark}（跑男认证 type=2 通过同步 is_runner=1） |
| 数据看板 | `/api/admin/dashboard` | summary / trend?days=7 / category / errand-peak |
| 用户管理 | `/api/admin/user` | GET list?keyword&status&page&size ・ POST ban {id} ・ POST unban {id} ・ GET credit-logs?userId&page&size |
| 商品监管 | `/api/admin/goods` | GET list?keyword&status&page&size ・ POST takedown {id,reason}（原因存 goods.violation_reason，status=4） |
| 跑腿监控 | `/api/admin/errand` | GET list?status&page&size（附双方昵称 + abnormal 异常标记：申诉中或超时未完结） |
| 投诉仲裁 | `/api/admin/complaint` | GET list?status&page&size ・ GET {id}（附订单快照） ・ POST handle {id,result,defendantCreditDelta}（信用分走 CreditService 流水） |
| 基础数据 | `/api/admin/location`、`/api/admin/notice` | GET location/list ・ POST location/save（有id改无id增） ・ POST location/toggle {id}；GET notice/list?page&size ・ POST notice/publish {title,content} ・ POST notice/offline {id} |

> 注意：管理端功能为 `goods` 表新增 `violation_reason` 列、为 `user_auth` 表新增 `type` 列。
> 已建库的老环境请执行：
> `ALTER TABLE goods ADD COLUMN violation_reason VARCHAR(255) DEFAULT NULL COMMENT '违规下架原因' AFTER status;`
> `ALTER TABLE user_auth ADD COLUMN type TINYINT NOT NULL DEFAULT 1 COMMENT '认证类型：1普通认证 2跑男认证' AFTER user_id;`

开放接口（无需 token）：`/api/user/login`、`/api/user/register`、`/api/user/sms`、
`/api/notice/list`、`/api/category/list`、`/api/location/list`，其余全部需登录。

## 核心设计（答辩考点）

1. **跑腿状态机**（`ErrandStateMachine` + `ErrandStatusEnum`）
   - 7 个状态 + 显式流转表 `TRANSITIONS`，杜绝"野流转"，新增状态只改一处
   - `transit()` 统一入口：幂等（current==target 直接返回）→ 流转校验 → 身份校验
     → 副作用 → 乐观锁 `UPDATE ... WHERE status=旧状态 AND version=旧版本`，rows=0 抛"手慢了"
   - 设计红线：DELIVERING 之后不允许单方面取消，取消必须走申诉仲裁

2. **抢单并发安全**（`ErrandOrderMapper.acceptOrder`）
   - `UPDATE ... WHERE status=0` 单条原子 CAS，MySQL 行锁保证只有一个赢家
   - 禁止"先查后改"（SELECT + UPDATE 之间的时间窗口会导致一单两人接）

3. **信用分只走流水**（`CreditService`）
   - `GREATEST(0, LEAST(100, credit_score + delta))` 上下限保护
   - 先改分、再写 `credit_log` 流水，同一事务，禁止直接 UPDATE credit_score
   - 扣分场景：跑男已接单后取消 -10；完成订单双方 +2；低于 60 限制发布/接单

4. **详情接口服务端脱敏**（`ErrandServiceImpl.detail`）
   - 取件码：发单人订单未结束可见；跑男仅 ACCEPTED/DELIVERING 可见；结束后都不返回
   - 联系方式：仅订单进行中的双方互相可见，路人不可见

5. **闲置下单锁定商品**（`GoodsOrderServiceImpl.create`）
   - CAS `UPDATE goods SET status=2 WHERE id=? AND status=1`，同事务生成订单（金额快照）

## TODO / 降级实现（骨架阶段）

- 短信验证码：内存 Map 存储 + 日志模拟发送，接真实短信服务前需换 Redis
- 取件码：明文存储（文档要求加密），接口层脱敏已实现，加密留 TODO
- `MessageServiceImpl.sessions`：内存聚合实现，数据量大后改 SQL 分组
- `pushStatusChange`：状态变更通知仅打日志，未接消息表/推送通道
- ~~校园认证 / 跑男认证：只有提交接口，管理端审核不在骨架范围~~ ✅ 已实现（`/api/admin/auth/**`）
- 闲置订单完成后是否加信用分：留 TODO 待产品确认
- 文件上传：本地磁盘存储，生产建议对象存储
- 定时任务（ARRIVED 满 24h 自动确认、超时未接单提醒）：未实现
