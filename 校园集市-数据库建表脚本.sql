-- =====================================================================
-- 校园集市 · 数据库建表脚本
-- 版本：v1.1    数据库：MySQL 8.0    引擎：InnoDB    字符集：utf8mb4
-- 与《校园集市-数据库设计文档.md》严格对应，可直接在 MySQL 8.0+ 执行
-- v1.1 变更（管理端模块配套）：
--   1) user_auth 增加 type 列（1普通认证 2跑男认证，审核通过后置 user.is_runner=1）
--   2) goods 增加 violation_reason 列（违规下架原因）
-- =====================================================================

CREATE DATABASE IF NOT EXISTS campus_market
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE campus_market;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 3.1 用户表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  phone           VARCHAR(11)     NOT NULL COMMENT '手机号',
  password        VARCHAR(64)     NOT NULL COMMENT 'BCrypt 加密后的密码',
  nickname        VARCHAR(32)     NOT NULL DEFAULT '校园用户' COMMENT '昵称',
  avatar          VARCHAR(255)             DEFAULT NULL COMMENT '头像 URL',
  gender          TINYINT         NOT NULL DEFAULT 0 COMMENT '0未知 1男 2女',
  auth_status     TINYINT         NOT NULL DEFAULT 0 COMMENT '认证状态：0未认证 1审核中 2已认证 3已驳回',
  is_runner       TINYINT         NOT NULL DEFAULT 0 COMMENT '是否跑男：0否 1是（需二次认证）',
  credit_score    INT             NOT NULL DEFAULT 100 COMMENT '信用分，满分100，低于60限制发布',
  status          TINYINT         NOT NULL DEFAULT 0 COMMENT '账号状态：0正常 1封禁',
  last_login_time DATETIME                 DEFAULT NULL COMMENT '最近登录时间',
  create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_phone (phone),
  KEY idx_credit (credit_score)
) ENGINE = InnoDB COMMENT '用户表';

-- ---------------------------------------------------------------------
-- 3.2 校园认证表（与 user 分离：可多次提交，历史材料留痕）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_auth` (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id        BIGINT UNSIGNED NOT NULL COMMENT '关联用户',
  type           TINYINT         NOT NULL DEFAULT 1 COMMENT '认证类型：1普通认证 2跑男认证（通过后 user.is_runner=1）',
  student_no     VARCHAR(20)     NOT NULL COMMENT '学号（一个学号只能认证一个账号）',
  real_name      VARCHAR(16)     NOT NULL COMMENT '真实姓名',
  college        VARCHAR(32)              DEFAULT NULL COMMENT '学院/系',
  grade          VARCHAR(16)              DEFAULT NULL COMMENT '年级班级',
  dorm_building  VARCHAR(32)              DEFAULT NULL COMMENT '宿舍楼（用于同楼推荐）',
  material_url   VARCHAR(255)    NOT NULL COMMENT '认证材料图片（校园卡/教务截图）',
  audit_status   TINYINT         NOT NULL DEFAULT 0 COMMENT '0待审核 1通过 2驳回',
  audit_remark   VARCHAR(255)             DEFAULT NULL COMMENT '驳回原因',
  auditor_id     BIGINT UNSIGNED          DEFAULT NULL COMMENT '审核管理员',
  audit_time     DATETIME                 DEFAULT NULL COMMENT '审核时间',
  create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_no (student_no),
  KEY idx_user (user_id),
  KEY idx_audit_status (audit_status)
) ENGINE = InnoDB COMMENT '校园认证表';

-- ---------------------------------------------------------------------
-- 3.3 分类表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
  id     INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  name   VARCHAR(16)  NOT NULL COMMENT '分类名',
  icon   VARCHAR(255)          DEFAULT NULL COMMENT '图标 URL',
  type   TINYINT      NOT NULL DEFAULT 1 COMMENT '1闲置分类 2跑腿类型',
  sort   INT          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  status TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  PRIMARY KEY (id),
  KEY idx_type_status (type, status)
) ENGINE = InnoDB COMMENT '分类表';

-- ---------------------------------------------------------------------
-- 3.4 闲置商品表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `goods` (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id        BIGINT UNSIGNED NOT NULL COMMENT '卖家',
  title          VARCHAR(64)     NOT NULL COMMENT '商品标题',
  category_id    INT UNSIGNED    NOT NULL COMMENT '分类',
  price          DECIMAL(10, 2)  NOT NULL COMMENT '售价',
  original_price DECIMAL(10, 2)           DEFAULT NULL COMMENT '原价（展示几折用）',
  quality        TINYINT         NOT NULL DEFAULT 3 COMMENT '新旧：1全新 2九成新 3八成新 4有明显使用痕迹',
  description    TEXT                     DEFAULT NULL COMMENT '商品描述',
  images         JSON                     DEFAULT NULL COMMENT '图片URL数组，最多9张',
  location_id    INT UNSIGNED             DEFAULT NULL COMMENT '期望面交地点 → school_location',
  status         TINYINT         NOT NULL DEFAULT 1 COMMENT '0审核中 1在售 2已售出 3卖家下架 4违规下架',
  violation_reason VARCHAR(255)             DEFAULT NULL COMMENT '违规下架原因（管理端下架时写入）',
  view_count     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '浏览数',
  fav_count      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '收藏数',
  want_count     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '我想要次数',
  is_deleted     TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_status_category (status, category_id),
  KEY idx_user (user_id),
  FULLTEXT KEY ft_title_desc (title, description)
) ENGINE = InnoDB COMMENT '闲置商品表';

-- ---------------------------------------------------------------------
-- 3.5 闲置交易订单表
-- 约束：同一商品同一时刻只允许一条进行中订单（status IN 0,1），
--       由"下单时校验商品状态 + 更新商品状态"两步在同一事务内保证
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `goods_order` (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_no     VARCHAR(32)     NOT NULL COMMENT '订单号（日期+用户id+随机数）',
  goods_id     BIGINT UNSIGNED NOT NULL COMMENT '商品',
  buyer_id     BIGINT UNSIGNED NOT NULL COMMENT '买家',
  seller_id    BIGINT UNSIGNED NOT NULL COMMENT '卖家',
  deal_price   DECIMAL(10, 2)  NOT NULL COMMENT '成交价（下单时快照，防商品改价）',
  status       TINYINT         NOT NULL DEFAULT 0 COMMENT '0待卖家确认 1交易中 2已完成 3已取消 4申诉中',
  remark       VARCHAR(255)             DEFAULT NULL COMMENT '买家留言',
  confirm_time DATETIME                 DEFAULT NULL COMMENT '卖家确认时间',
  finish_time  DATETIME                 DEFAULT NULL COMMENT '完成时间（双方确认面交完成）',
  cancel_reason VARCHAR(128)            DEFAULT NULL COMMENT '取消原因',
  cancel_by    BIGINT UNSIGNED          DEFAULT NULL COMMENT '取消发起方',
  is_deleted   TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  update_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_buyer (buyer_id, status),
  KEY idx_seller (seller_id, status),
  KEY idx_goods (goods_id)
) ENGINE = InnoDB COMMENT '闲置交易订单表';

-- ---------------------------------------------------------------------
-- 3.6 跑腿订单表（核心表）
-- version 乐观锁：UPDATE ... WHERE status=旧状态 AND version=旧版本
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `errand_order` (
  id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_no             VARCHAR(32)     NOT NULL COMMENT '订单号',
  publisher_id         BIGINT UNSIGNED NOT NULL COMMENT '发单人',
  runner_id            BIGINT UNSIGNED          DEFAULT NULL COMMENT '接单人（跑男）',
  type                 TINYINT         NOT NULL COMMENT '1取快递 2代买餐 3代送物品 4其他',
  title                VARCHAR(64)     NOT NULL COMMENT '需求标题',
  pickup_location_id   INT UNSIGNED    NOT NULL COMMENT '取货地点 → school_location',
  delivery_location_id INT UNSIGNED    NOT NULL COMMENT '送达地点 → school_location',
  pickup_detail        VARCHAR(128)             DEFAULT NULL COMMENT '取货补充说明（如快递柜编号）',
  pickup_code          VARCHAR(255)             DEFAULT NULL COMMENT '取件码，加密存储，仅接单后对接单人可见',
  goods_desc           VARCHAR(255)             DEFAULT NULL COMMENT '物品描述（大小/重量/是否易碎）',
  reward               DECIMAL(10, 2)  NOT NULL COMMENT '悬赏金额',
  expect_time          DATETIME                 DEFAULT NULL COMMENT '期望完成时间',
  status               TINYINT         NOT NULL DEFAULT 0 COMMENT '0待接单 1已接单 2配送中 3送达待确认 4已完成 5已取消 6申诉中',
  version              INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，防并发抢单',
  accept_time          DATETIME                 DEFAULT NULL COMMENT '接单时间',
  deliver_time         DATETIME                 DEFAULT NULL COMMENT '开始配送时间',
  arrive_time          DATETIME                 DEFAULT NULL COMMENT '送达时间',
  finish_time          DATETIME                 DEFAULT NULL COMMENT '完成时间（发单人确认）',
  cancel_by            BIGINT UNSIGNED          DEFAULT NULL COMMENT '取消发起方',
  cancel_reason        VARCHAR(128)             DEFAULT NULL COMMENT '取消原因',
  is_deleted           TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  create_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  update_time          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_status_type (status, type),
  KEY idx_publisher (publisher_id, status),
  KEY idx_runner (runner_id, status)
) ENGINE = InnoDB COMMENT '跑腿订单表';

-- ---------------------------------------------------------------------
-- 3.7 评价表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `evaluation` (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_type   TINYINT         NOT NULL COMMENT '1闲置订单 2跑腿订单',
  order_id     BIGINT UNSIGNED NOT NULL COMMENT '关联订单',
  from_user_id BIGINT UNSIGNED NOT NULL COMMENT '评价人',
  to_user_id   BIGINT UNSIGNED NOT NULL COMMENT '被评价人',
  score        TINYINT         NOT NULL DEFAULT 5 COMMENT '评分 1~5',
  tags         JSON                     DEFAULT NULL COMMENT '标签数组',
  content      VARCHAR(500)             DEFAULT NULL COMMENT '文字评价',
  create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_from (order_type, order_id, from_user_id),
  KEY idx_to_user (to_user_id)
) ENGINE = InnoDB COMMENT '评价表';

-- ---------------------------------------------------------------------
-- 3.8 信用分流水表（信用分只能通过本表累加更新，可追溯）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `credit_log` (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id      BIGINT UNSIGNED NOT NULL COMMENT '变动用户',
  change_value INT             NOT NULL COMMENT '变动值（正加负减）',
  balance      INT             NOT NULL COMMENT '变动后余额分',
  reason       VARCHAR(128)    NOT NULL COMMENT '变动原因',
  order_type   TINYINT                  DEFAULT NULL COMMENT '关联订单类型',
  order_id     BIGINT UNSIGNED          DEFAULT NULL COMMENT '关联订单',
  operator_id  BIGINT UNSIGNED          DEFAULT NULL COMMENT '操作人，NULL=系统自动',
  create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (id),
  KEY idx_user (user_id, create_time)
) ENGINE = InnoDB COMMENT '信用分流水表';

-- ---------------------------------------------------------------------
-- 3.9 投诉表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `complaint` (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  order_type   TINYINT         NOT NULL COMMENT '1闲置 2跑腿',
  order_id     BIGINT UNSIGNED NOT NULL COMMENT '关联订单',
  plaintiff_id BIGINT UNSIGNED NOT NULL COMMENT '投诉人（原告）',
  defendant_id BIGINT UNSIGNED NOT NULL COMMENT '被投诉人（被告）',
  type         TINYINT         NOT NULL COMMENT '1爽约 2商品与描述不符 3物品损坏 4态度恶劣 5其他',
  content      TEXT            NOT NULL COMMENT '投诉描述',
  evidence     JSON                     DEFAULT NULL COMMENT '证据图片数组',
  status       TINYINT         NOT NULL DEFAULT 0 COMMENT '0待处理 1处理中 2已办结',
  result       VARCHAR(255)             DEFAULT NULL COMMENT '处理结果',
  handler_id   BIGINT UNSIGNED          DEFAULT NULL COMMENT '处理管理员',
  handle_time  DATETIME                 DEFAULT NULL COMMENT '处理时间',
  create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '投诉时间',
  PRIMARY KEY (id),
  KEY idx_status (status),
  KEY idx_plaintiff (plaintiff_id),
  KEY idx_defendant (defendant_id),
  KEY idx_order (order_type, order_id)
) ENGINE = InnoDB COMMENT '投诉表';

-- ---------------------------------------------------------------------
-- 3.10 收藏表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `favorite` (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id     BIGINT UNSIGNED NOT NULL COMMENT '用户',
  goods_id    BIGINT UNSIGNED NOT NULL COMMENT '商品',
  create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_goods (user_id, goods_id)
) ENGINE = InnoDB COMMENT '收藏表';

-- ---------------------------------------------------------------------
-- 3.11 聊天消息表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  from_user_id BIGINT UNSIGNED NOT NULL COMMENT '发送方',
  to_user_id   BIGINT UNSIGNED NOT NULL COMMENT '接收方',
  goods_id     BIGINT UNSIGNED          DEFAULT NULL COMMENT '关联商品（从商品页发起会话时记录）',
  type         TINYINT         NOT NULL DEFAULT 1 COMMENT '1文本 2图片',
  content      VARCHAR(1000)   NOT NULL COMMENT '内容',
  is_read      TINYINT         NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (id),
  KEY idx_session (from_user_id, to_user_id),
  KEY idx_unread (to_user_id, is_read)
) ENGINE = InnoDB COMMENT '聊天消息表';

-- ---------------------------------------------------------------------
-- 3.12 校内地点表（平台预置，用户只能选不能填）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `school_location` (
  id     INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  name   VARCHAR(32)  NOT NULL COMMENT '地点名',
  type   TINYINT      NOT NULL COMMENT '1宿舍楼 2教学楼 3快递点 4食堂 5其他',
  sort   INT          NOT NULL DEFAULT 0 COMMENT '排序',
  status TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  PRIMARY KEY (id),
  KEY idx_type_status (type, status)
) ENGINE = InnoDB COMMENT '校内地点表';

-- ---------------------------------------------------------------------
-- 3.13 系统公告表
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `notice` (
  id          INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  title       VARCHAR(64)  NOT NULL COMMENT '标题',
  content     TEXT         NOT NULL COMMENT '内容',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1发布 0下线',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (id),
  KEY idx_status (status, create_time)
) ENGINE = InnoDB COMMENT '系统公告表';

-- =====================================================================
-- 初始数据
-- =====================================================================

-- 闲置分类（type=1）
INSERT INTO `category` (name, type, sort, status) VALUES
  ('教材',   1, 1, 1),
  ('数码',   1, 2, 1),
  ('服饰',   1, 3, 1),
  ('生活用品', 1, 4, 1),
  ('运动器材', 1, 5, 1),
  ('其他',   1, 6, 1);

-- 跑腿类型（type=2，与 errand_order.type 枚举一致）
INSERT INTO `category` (name, type, sort, status) VALUES
  ('取快递',  2, 1, 1),
  ('代买餐',  2, 2, 1),
  ('代送物品', 2, 3, 1),
  ('其他',    2, 4, 1);

-- 校内地点（示例数据，按实际学校情况调整）
INSERT INTO `school_location` (name, type, sort, status) VALUES
  ('1号宿舍楼', 1, 1, 1),
  ('2号宿舍楼', 1, 2, 1),
  ('3号宿舍楼', 1, 3, 1),
  ('4号宿舍楼', 1, 4, 1),
  ('5号宿舍楼', 1, 5, 1),
  ('第一教学楼', 2, 10, 1),
  ('第二教学楼', 2, 11, 1),
  ('图书馆',    2, 12, 1),
  ('菜鸟驿站',  3, 20, 1),
  ('顺丰快递点', 3, 21, 1),
  ('一食堂',    4, 30, 1),
  ('二食堂',    4, 31, 1),
  ('校门口',    5, 40, 1),
  ('操场',      5, 41, 1);

-- 系统公告（示例）
INSERT INTO `notice` (title, content, status) VALUES
  ('欢迎使用校园集市', '平台仅限本校实名认证同学使用。交易请在校内公共场所面交，注意人身与财物安全。', 1),
  ('跑腿接单规范', '接单后请按时完成配送，爽约将扣除 10 信用分。信用分低于 60 将被限制发布与接单。', 1);
