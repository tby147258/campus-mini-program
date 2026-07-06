# 校园综合服务平台 - 数据库设计

## 文件结构

```
docs/database/
├── README.md                  # 本文件
├── schema.sql                 # 完整建表SQL脚本（7张表 + 22个索引 + 注释）
├── test_data.sql              # 测试数据SQL（覆盖核心业务场景）
├── data_dictionary.md         # 数据字典（含字段清单、索引、外键）
├── er_diagram.svg             # E-R图（7张表的实体关系）
└── 数据库设计说明书.docx       # 数据库设计文档（Word格式，含完整表结构）
```

## 数据库信息

| 项目 | 值 |
|------|-----|
| 数据库名 | campus |
| 字符集 | utf8mb4 |
| 排序规则 | utf8mb4_general_ci |
| 存储引擎 | InnoDB |
| 表数量 | 7 |
| 索引数量 | 22 |

## 表清单

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| user | 用户表（学生/管理员） | open_id, password, nickname, role |
| announcement | 公告表 | title, content, category, publisher_id |
| lost_found | 失物信息表 | type, item_name, status, auditor_id |
| repair_order | 报修工单表 | order_no, repair_type, status, contact_person |
| repair_log | 工单处理记录表 | order_id, action, from_status, to_status |
| operation_log | 操作日志表 | module, action, ip_address |
| system_config | 系统配置表 | config_key, config_value |

## E-R图

![E-R图](er_diagram.svg)

*图：7张表的实体关系图，含8条外键关联（system_config为独立配置表）*

## 执行建表

```bash
# 方法一：命令行导入
mysql -u root -p < docs/database/schema.sql
mysql -u root -p < docs/database/test_data.sql

# 方法二：MySQL命令行内执行
mysql -u root -p
source path/to/schema.sql;
source path/to/test_data.sql;
```

## 索引方案

- 7张表共 **22 个索引**（含主键、唯一索引、普通索引、复合索引、全文索引）
- 覆盖所有高频查询场景
- 复合索引优先：`idx_status_created`(工单列表)、`idx_type_status`(失物筛选)、`idx_module_action`(日志审计)
- 全文索引：`ft_title_content`(公告搜索)
- 逾期提醒：`idx_estimated_complete`(报修工单)

## 逻辑外键关系

| 子表 | 外键 | 父表 | 业务含义 |
|------|------|------|----------|
| announcement | publisher_id | user | 公告发布人 |
| lost_found | user_id | user | 发布人 |
| lost_found | auditor_id | user | 审核人 |
| repair_order | user_id | user | 提交人 |
| repair_order | handler_id | user | 处理人 |
| repair_log | order_id | repair_order | 所属工单 |
| repair_log | operator_id | user | 操作人 |
| operation_log | user_id | user | 操作人 |