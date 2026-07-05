# 校园综合服务平台 - 数据库设计

## 文件结构

```
docs/database/
├── README.md                  # 本文件
├── schema.sql                 # 完整建表SQL脚本（6张表 + 索引 + 注释）
├── test_data.sql              # 测试数据SQL（覆盖核心业务场景）
└── 数据库设计说明书.docx       # 数据库设计文档（Word格式）
```

## 数据库信息

| 项目 | 值 |
|------|-----|
| 数据库名 | campus |
| 字符集 | utf8mb4 |
| 排序规则 | utf8mb4_general_ci |
| 存储引擎 | InnoDB |
| 表数量 | 6 |

## 表清单

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| user | 用户表（学生/管理员） | open_id, nickname, role |
| announcement | 公告表 | title, content, category |
| lost_found | 失物信息表 | type, item_name, status |
| repair_order | 报修工单表 | order_no, repair_type, status |
| repair_log | 工单处理记录表 | order_id, action, to_status |
| operation_log | 操作日志表 | module, action, ip_address |

## 执行建表

```bash
# 登录MySQL
mysql -u root -p

# 执行建表脚本
source path/to/schema.sql;

# 导入测试数据
source path/to/test_data.sql;
```

## 索引方案

- 6张表共 19 个索引（含主键、唯一索引、普通索引、复合索引、全文索引）
- 覆盖所有高频查询场景
- 复合索引优先：`idx_status_created`(工单列表)、`idx_type_status`(失物筛选)、`idx_module_action`(日志审计)
- 全文索引：`ft_title_content`(公告搜索)