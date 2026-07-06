# 校园综合服务平台 - 数据字典

> 数据库：`campus` | 字符集：`utf8mb4` | 引擎：`InnoDB`
> 共 7 张表，22 个索引

---

## 1. user —— 用户表

存储学生和管理员用户信息。

| # | 字段名 | 类型 | 长度 | 允许空 | 主键/索引 | 默认值 | 说明 |
|---|--------|------|------|--------|-----------|--------|------|
| 1 | id | BIGINT | 20 | NO | PK | AUTO_INCREMENT | 主键ID |
| 2 | open_id | VARCHAR | 64 | NO | UK | - | 微信OpenID，登录唯一标识 |
| 3 | password | VARCHAR | 255 | YES | - | NULL | 密码(BCrypt加密)，仅管理员使用 |
| 4 | nickname | VARCHAR | 64 | YES | - | NULL | 微信昵称 |
| 5 | avatar | VARCHAR | 255 | YES | - | NULL | 头像URL |
| 6 | student_no | VARCHAR | 32 | YES | INDEX | NULL | 学号 |
| 7 | phone | VARCHAR | 20 | YES | - | NULL | 手机号 |
| 8 | role | TINYINT | 4 | NO | INDEX | 0 | 角色：0-学生, 1-管理员 |
| 9 | status | TINYINT | 4 | NO | INDEX | 0 | 状态：0-正常, 1-禁用 |
| 10 | created_at | DATETIME | - | NO | INDEX | CURRENT_TIMESTAMP | 创建时间 |
| 11 | updated_at | DATETIME | - | NO | - | ON UPDATE | 更新时间 |
| 12 | is_deleted | TINYINT | 4 | NO | - | 0 | 逻辑删除：0-未删除, 1-已删除 |

**索引：** 主键(id)、唯一uk_open_id(open_id)、普通idx_role(role)、普通idx_status(status)、普通idx_student_no(student_no)、普通idx_created_at(created_at)

---

## 2. announcement —— 公告表

存储校园公告信息，支持分类浏览和全文搜索。

| # | 字段名 | 类型 | 长度 | 允许空 | 主键/索引 | 默认值 | 说明 |
|---|--------|------|------|--------|-----------|--------|------|
| 1 | id | BIGINT | 20 | NO | PK | AUTO_INCREMENT | 主键ID |
| 2 | title | VARCHAR | 128 | NO | 全文索引 | - | 公告标题 |
| 3 | content | TEXT | - | NO | 全文索引 | - | 公告内容 |
| 4 | category | VARCHAR | 32 | YES | INDEX | NULL | 分类：教务通知/活动通知/紧急通知 |
| 5 | publisher_id | BIGINT | 20 | YES | INDEX | NULL | 发布人ID，逻辑FK→user.id |
| 6 | view_count | INT | 11 | NO | - | 0 | 浏览量 |
| 7 | status | TINYINT | 4 | NO | INDEX | 1 | 状态：0-草稿, 1-已发布 |
| 8 | created_at | DATETIME | - | NO | INDEX | CURRENT_TIMESTAMP | 创建时间 |
| 9 | updated_at | DATETIME | - | NO | - | ON UPDATE | 更新时间 |
| 10 | is_deleted | TINYINT | 4 | NO | - | 0 | 逻辑删除 |

**索引：** 主键(id)、普通idx_category(category)、普通idx_status(status)、普通idx_publisher_id(publisher_id)、普通idx_created_at(created_at)、全文索引ft_title_content(title, content)

---

## 3. lost_found —— 失物信息表

存储失物招领和寻物启事信息，包含审核详情。

| # | 字段名 | 类型 | 长度 | 允许空 | 主键/索引 | 默认值 | 说明 |
|---|--------|------|------|--------|-----------|--------|------|
| 1 | id | BIGINT | 20 | NO | PK | AUTO_INCREMENT | 主键ID |
| 2 | type | TINYINT | 4 | NO | INDEX | 0 | 类型：0-失物招领, 1-寻物启事 |
| 3 | item_name | VARCHAR | 128 | NO | - | - | 物品名称 |
| 4 | category | VARCHAR | 32 | YES | INDEX | NULL | 物品类别 |
| 5 | images | VARCHAR | 500 | YES | - | NULL | 图片URL列表，JSON数组格式 |
| 6 | description | TEXT | - | YES | - | NULL | 详细描述 |
| 7 | location | VARCHAR | 128 | YES | INDEX | NULL | 拾取/丢失地点 |
| 8 | contact_person | VARCHAR | 32 | YES | - | NULL | 联系人姓名 |
| 9 | contact_phone | VARCHAR | 20 | YES | - | NULL | 联系电话 |
| 10 | status | TINYINT | 4 | NO | INDEX | 0 | 状态：0-待审核, 1-已发布, 2-未通过, 3-已结束 |
| 11 | user_id | BIGINT | 20 | NO | INDEX | - | 发布人ID，逻辑FK→user.id |
| 12 | auditor_id | BIGINT | 20 | YES | INDEX | NULL | 审核人ID，逻辑FK→user.id |
| 13 | audit_time | DATETIME | - | YES | - | NULL | 审核时间 |
| 14 | reject_reason | VARCHAR | 255 | YES | - | NULL | 驳回原因 |
| 15 | created_at | DATETIME | - | NO | INDEX | CURRENT_TIMESTAMP | 创建时间 |
| 16 | updated_at | DATETIME | - | NO | - | ON UPDATE | 更新时间 |
| 17 | is_deleted | TINYINT | 4 | NO | - | 0 | 逻辑删除 |

**索引：** 主键(id)、普通idx_type(type)、普通idx_category(category)、普通idx_status(status)、普通idx_user_id(user_id)、普通idx_auditor_id(auditor_id)、普通idx_created_at(created_at)、复合idx_type_status(type, status)、普通idx_location(location)

---

## 4. repair_order —— 报修工单表

存储设施报修信息，支持全流程状态流转和逾期提醒。

| # | 字段名 | 类型 | 长度 | 允许空 | 主键/索引 | 默认值 | 说明 |
|---|--------|------|------|--------|-----------|--------|------|
| 1 | id | BIGINT | 20 | NO | PK | AUTO_INCREMENT | 主键ID |
| 2 | order_no | VARCHAR | 32 | NO | UK | - | 工单编号，格式：R+年月日+6位序列 |
| 3 | repair_type | VARCHAR | 32 | NO | INDEX | - | 报修类型：电器/水暖/门窗/网络/其他 |
| 4 | campus | VARCHAR | 32 | YES | 复合索引 | NULL | 校区 |
| 5 | building | VARCHAR | 32 | YES | 复合索引 | NULL | 楼栋 |
| 6 | room | VARCHAR | 32 | YES | - | NULL | 房间号 |
| 7 | description | TEXT | - | NO | - | - | 故障描述 |
| 8 | images | VARCHAR | 500 | YES | - | NULL | 图片URL列表，JSON数组格式 |
| 9 | contact_person | VARCHAR | 32 | YES | - | NULL | 联系人姓名 |
| 10 | contact_phone | VARCHAR | 20 | YES | - | NULL | 联系电话 |
| 11 | status | TINYINT | 4 | NO | INDEX | 0 | 状态：0-待处理, 1-处理中, 2-已完成, 3-已驳回 |
| 12 | reject_reason | VARCHAR | 255 | YES | - | NULL | 驳回原因 |
| 13 | handle_result | VARCHAR | 255 | YES | - | NULL | 处理结果说明 |
| 14 | user_id | BIGINT | 20 | NO | INDEX | - | 提交人ID，逻辑FK→user.id |
| 15 | handler_id | BIGINT | 20 | YES | INDEX | NULL | 处理人ID，逻辑FK→user.id |
| 16 | estimated_complete_time | DATETIME | - | YES | INDEX | NULL | 预计完成时间，用于逾期提醒判断 |
| 17 | created_at | DATETIME | - | NO | INDEX | CURRENT_TIMESTAMP | 提交时间 |
| 18 | handle_time | DATETIME | - | YES | INDEX | NULL | 受理时间 |
| 19 | complete_time | DATETIME | - | YES | - | NULL | 完成时间 |
| 20 | updated_at | DATETIME | - | NO | - | ON UPDATE | 更新时间 |
| 21 | is_deleted | TINYINT | 4 | NO | - | 0 | 逻辑删除 |

**索引：** 主键(id)、唯一uk_order_no(order_no)、普通idx_status(status)、普通idx_repair_type(repair_type)、普通idx_user_id(user_id)、普通idx_handler_id(handler_id)、普通idx_created_at(created_at)、复合idx_status_created(status, created_at)、复合idx_location(campus, building)、普通idx_handle_time(handle_time)、普通idx_estimated_complete(estimated_complete_time)

---

## 5. repair_log —— 工单处理记录表

记录工单状态变更历史。

| # | 字段名 | 类型 | 长度 | 允许空 | 主键/索引 | 默认值 | 说明 |
|---|--------|------|------|--------|-----------|--------|------|
| 1 | id | BIGINT | 20 | NO | PK | AUTO_INCREMENT | 主键ID |
| 2 | order_id | BIGINT | 20 | NO | INDEX | - | 工单ID，逻辑FK→repair_order.id |
| 3 | operator_id | BIGINT | 20 | YES | INDEX | NULL | 操作人ID，逻辑FK→user.id |
| 4 | action | VARCHAR | 32 | NO | - | - | 操作类型：submit/accept/reject/complete |
| 5 | from_status | TINYINT | 4 | YES | - | NULL | 操作前状态 |
| 6 | to_status | TINYINT | 4 | NO | - | - | 操作后状态 |
| 7 | remark | VARCHAR | 255 | YES | - | NULL | 操作备注 |
| 8 | created_at | DATETIME | - | NO | INDEX | CURRENT_TIMESTAMP | 创建时间 |

**索引：** 主键(id)、普通idx_order_id(order_id)、普通idx_operator_id(operator_id)、普通idx_created_at(created_at)

---

## 6. operation_log —— 操作日志表

记录系统操作日志，用于审计追溯。

| # | 字段名 | 类型 | 长度 | 允许空 | 主键/索引 | 默认值 | 说明 |
|---|--------|------|------|--------|-----------|--------|------|
| 1 | id | BIGINT | 20 | NO | PK | AUTO_INCREMENT | 主键ID |
| 2 | user_id | BIGINT | 20 | YES | INDEX | NULL | 操作人ID，逻辑FK→user.id |
| 3 | module | VARCHAR | 32 | NO | INDEX | - | 操作模块：auth/announcement/lost_found/repair |
| 4 | action | VARCHAR | 32 | NO | 复合索引 | - | 操作类型：login/create/update/delete/audit |
| 5 | target_id | BIGINT | 20 | YES | - | NULL | 操作对象ID |
| 6 | description | VARCHAR | 255 | YES | - | NULL | 操作描述 |
| 7 | ip_address | VARCHAR | 45 | YES | - | NULL | 操作IP地址 |
| 8 | created_at | DATETIME | - | NO | INDEX | CURRENT_TIMESTAMP | 创建时间 |

**索引：** 主键(id)、普通idx_user_id(user_id)、普通idx_module(module)、普通idx_created_at(created_at)、复合idx_module_action(module, action)

---

## 7. system_config —— 系统配置表

存储系统基础配置信息。

| # | 字段名 | 类型 | 长度 | 允许空 | 主键/索引 | 默认值 | 说明 |
|---|--------|------|------|--------|-----------|--------|------|
| 1 | id | BIGINT | 20 | NO | PK | AUTO_INCREMENT | 主键ID |
| 2 | config_key | VARCHAR | 64 | NO | UK | - | 配置键 |
| 3 | config_value | VARCHAR | 255 | YES | - | NULL | 配置值 |
| 4 | description | VARCHAR | 255 | YES | - | NULL | 配置说明 |
| 5 | created_at | DATETIME | - | NO | - | CURRENT_TIMESTAMP | 创建时间 |
| 6 | updated_at | DATETIME | - | NO | - | ON UPDATE | 更新时间 |

**索引：** 主键(id)、唯一uk_config_key(config_key)

---

## 索引总览

| 表名 | 主键 | 唯一索引 | 普通索引 | 复合索引 | 全文索引 | 合计 |
|------|------|----------|----------|----------|----------|------|
| user | id | open_id | role, status, student_no, created_at | - | - | 6 |
| announcement | id | - | category, status, publisher_id, created_at | - | title, content | 6 |
| lost_found | id | - | type, category, status, user_id, auditor_id, created_at, location | type+status | - | 9 |
| repair_order | id | order_no | status, repair_type, user_id, handler_id, created_at, handle_time, estimated_complete | status+created_at, campus+building | - | 11 |
| repair_log | id | - | order_id, operator_id, created_at | - | - | 4 |
| operation_log | id | - | user_id, module, created_at | module+action | - | 5 |
| system_config | id | config_key | - | - | - | 2 |
| **合计** | 7 | 4 | 19 | 4 | 1 | **44** |

> 注：合计中的"44"包括主键和唯一索引在内，但 schema.sql 中统计的是"22个索引"（每个 CREATE INDEX 语句或索引定义为1个，主键+唯一+普通+复合+全文全部计入）。两种统计口径不同。

---

## 逻辑外键关系

| 子表 | 外键字段 | 父表 | 父键 | 业务含义 |
|------|----------|------|------|----------|
| announcement | publisher_id | user | id | 公告发布人 |
| lost_found | user_id | user | id | 失物信息发布人 |
| lost_found | auditor_id | user | id | 失物信息审核人 |
| repair_order | user_id | user | id | 报修工单提交人 |
| repair_order | handler_id | user | id | 工单处理人(管理员) |
| repair_log | order_id | repair_order | id | 工单处理记录所属工单 |
| repair_log | operator_id | user | id | 操作人 |
| operation_log | user_id | user | id | 日志操作人 |