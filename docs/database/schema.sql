-- ============================================================
-- 校园综合服务平台 - 数据库建表脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_general_ci
-- 引擎: InnoDB
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE campus;

-- ============================================================
-- 1. 用户表 (user)
--    存储学生和管理员用户信息
-- ============================================================
CREATE TABLE IF NOT EXISTS user (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    open_id     VARCHAR(64)  NOT NULL                 COMMENT '微信OpenID',
    nickname    VARCHAR(64)  DEFAULT NULL             COMMENT '微信昵称',
    avatar      VARCHAR(255) DEFAULT NULL             COMMENT '头像URL',
    student_no  VARCHAR(32)  DEFAULT NULL             COMMENT '学号',
    phone       VARCHAR(20)  DEFAULT NULL             COMMENT '手机号',
    role        TINYINT      NOT NULL DEFAULT 0       COMMENT '角色：0-学生, 1-管理员',
    status      TINYINT      NOT NULL DEFAULT 0       COMMENT '状态：0-正常, 1-禁用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_open_id (open_id),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_student_no (student_no),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';


-- ============================================================
-- 2. 公告表 (announcement)
--    存储校园公告信息
-- ============================================================
CREATE TABLE IF NOT EXISTS announcement (
    id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    title        VARCHAR(128) NOT NULL                 COMMENT '公告标题',
    content      TEXT         NOT NULL                 COMMENT '公告内容',
    category     VARCHAR(32)  DEFAULT NULL             COMMENT '分类：教务通知/活动通知/紧急通知',
    publisher_id BIGINT       DEFAULT NULL             COMMENT '发布人ID，关联user.id',
    view_count   INT          NOT NULL DEFAULT 0       COMMENT '浏览量',
    status       TINYINT      NOT NULL DEFAULT 1       COMMENT '状态：0-草稿, 1-已发布',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted   TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_publisher_id (publisher_id),
    INDEX idx_created_at (created_at),
    FULLTEXT INDEX ft_title_content (title, content)   -- 全文索引支持公告搜索
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';


-- ============================================================
-- 3. 失物信息表 (lost_found)
--    存储失物招领和寻物启事信息
-- ============================================================
CREATE TABLE IF NOT EXISTS lost_found (
    id             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    type           TINYINT      NOT NULL DEFAULT 0       COMMENT '类型：0-失物招领, 1-寻物启事',
    item_name      VARCHAR(128) NOT NULL                 COMMENT '物品名称',
    category       VARCHAR(32)  DEFAULT NULL             COMMENT '物品类别',
    images         VARCHAR(500) DEFAULT NULL             COMMENT '图片URL列表，JSON数组格式',
    description    TEXT                                   COMMENT '详细描述',
    location       VARCHAR(128) DEFAULT NULL             COMMENT '拾取/丢失地点',
    contact_person VARCHAR(32)  DEFAULT NULL             COMMENT '联系人姓名',
    contact_phone  VARCHAR(20)  DEFAULT NULL             COMMENT '联系电话',
    status         TINYINT      NOT NULL DEFAULT 0       COMMENT '状态：0-待审核, 1-已发布, 2-未通过, 3-已结束',
    user_id        BIGINT       NOT NULL                 COMMENT '发布人ID，关联user.id',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted     TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (id),
    INDEX idx_type (type),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_type_status (type, status),               -- 复合索引：按类型+状态查询
    INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='失物信息表';


-- ============================================================
-- 4. 报修工单表 (repair_order)
--    存储设施报修工单信息
-- ============================================================
CREATE TABLE IF NOT EXISTS repair_order (
    id             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    order_no       VARCHAR(32)  NOT NULL                 COMMENT '工单编号，格式：R+年月日+6位序列',
    repair_type    VARCHAR(32)  NOT NULL                 COMMENT '报修类型：电器/水暖/门窗/网络/其他',
    campus         VARCHAR(32)  DEFAULT NULL             COMMENT '校区',
    building       VARCHAR(32)  DEFAULT NULL             COMMENT '楼栋',
    room           VARCHAR(32)  DEFAULT NULL             COMMENT '房间号',
    description    TEXT         NOT NULL                 COMMENT '故障描述',
    images         VARCHAR(500) DEFAULT NULL             COMMENT '图片URL列表，JSON数组格式',
    contact_phone  VARCHAR(20)  DEFAULT NULL             COMMENT '联系电话',
    status         TINYINT      NOT NULL DEFAULT 0       COMMENT '状态：0-待处理, 1-处理中, 2-已完成, 3-已驳回',
    reject_reason  VARCHAR(255) DEFAULT NULL             COMMENT '驳回原因',
    handle_result  VARCHAR(255) DEFAULT NULL             COMMENT '处理结果说明',
    user_id        BIGINT       NOT NULL                 COMMENT '提交人ID，关联user.id',
    handler_id     BIGINT       DEFAULT NULL             COMMENT '处理人ID，关联user.id',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    handle_time    DATETIME     DEFAULT NULL             COMMENT '受理时间',
    complete_time  DATETIME     DEFAULT NULL             COMMENT '完成时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted     TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_repair_type (repair_type),
    INDEX idx_user_id (user_id),
    INDEX idx_handler_id (handler_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status_created (status, created_at),      -- 复合索引：按状态+时间排序查询
    INDEX idx_location (campus, building),              -- 复合索引：按地点查询
    INDEX idx_handle_time (handle_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报修工单表';


-- ============================================================
-- 5. 工单处理记录表 (repair_log)
--    记录工单状态变更历史
-- ============================================================
CREATE TABLE IF NOT EXISTS repair_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    order_id    BIGINT       NOT NULL                 COMMENT '工单ID，关联repair_order.id',
    operator_id BIGINT       DEFAULT NULL             COMMENT '操作人ID，关联user.id',
    action      VARCHAR(32)  NOT NULL                 COMMENT '操作类型：submit/accept/reject/complete',
    from_status TINYINT      DEFAULT NULL             COMMENT '操作前状态',
    to_status   TINYINT      NOT NULL                 COMMENT '操作后状态',
    remark      VARCHAR(255) DEFAULT NULL             COMMENT '操作备注',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_order_id (order_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单处理记录表';


-- ============================================================
-- 6. 操作日志表 (operation_log)
--    记录系统操作日志，用于审计追溯
-- ============================================================
CREATE TABLE IF NOT EXISTS operation_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    user_id       BIGINT       DEFAULT NULL             COMMENT '操作人ID，关联user.id',
    module        VARCHAR(32)  NOT NULL                 COMMENT '操作模块：auth/announcement/lost_found/repair',
    action        VARCHAR(32)  NOT NULL                 COMMENT '操作类型：login/create/update/delete/audit',
    target_id     BIGINT       DEFAULT NULL             COMMENT '操作对象ID',
    description   VARCHAR(255) DEFAULT NULL             COMMENT '操作描述',
    ip_address    VARCHAR(45)  DEFAULT NULL             COMMENT '操作IP地址',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_created_at (created_at),
    INDEX idx_module_action (module, action)            -- 复合索引：按模块+操作类型查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';


-- ============================================================
-- 索引方案总结
-- ============================================================
-- user表:
--   - 主键: id
--   - 唯一索引: open_id (微信登录核心查询)
--   - 普通索引: role, status, student_no, created_at
--
-- announcement表:
--   - 主键: id
--   - 普通索引: category, status, publisher_id, created_at
--   - 全文索引: title+content (支持公告搜索)
--
-- lost_found表:
--   - 主键: id
--   - 普通索引: type, category, status, user_id, created_at, location
--   - 复合索引: type+status (高频查询：按类型+状态筛选)
--
-- repair_order表:
--   - 主键: id
--   - 唯一索引: order_no (工单编号查询)
--   - 普通索引: status, repair_type, user_id, handler_id, created_at, handle_time
--   - 复合索引: status+created_at (工单列表按状态+时间排序)
--   - 复合索引: campus+building (按地点查询工单)
--
-- repair_log表:
--   - 主键: id
--   - 普通索引: order_id, operator_id, created_at
--
-- operation_log表:
--   - 主键: id
--   - 普通索引: user_id, module, created_at
--   - 复合索引: module+action (按模块+操作类型查询)
--
-- ============================================================
-- 外键关系说明（MyBatis-Plus不强制使用物理外键，通过逻辑关联）
-- ============================================================
-- user.id ← announcement.publisher_id
-- user.id ← lost_found.user_id
-- user.id ← repair_order.user_id
-- user.id ← repair_order.handler_id
-- repair_order.id ← repair_log.order_id
-- user.id ← repair_log.operator_id
-- user.id ← operation_log.user_id
--