# 校园综合服务平台 🎓

## 项目简介

**校园综合服务平台**是一个面向高校师生的多功能微信小程序+Web管理后台项目。项目旨在解决校园信息分散、失物招领匹配困难、设施报修流程繁琐等痛点，整合校园公告、失物招领、设施报修、天气查询等核心服务，打造一站式校园综合服务平台。

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| **小程序前端** | 微信小程序原生框架 | 学生端移动应用 |
| **管理后台** | Vue 3 + Element Plus + Pinia | 管理员Web端 |
| **后端服务** | Spring Boot 3.2 + MyBatis-Plus | RESTful API |
| **数据库** | MySQL 8.0 | 关系型数据库 |
| **开发工具** | Trae IDE (DeepSeek-V4-Flash) | AI辅助开发 |
| **天气服务** | 和风天气API | 实时天气查询 |
| **认证鉴权** | JWT (JJWT 0.12.x) + BCrypt | Token认证与密码加密 |
| **缓存** | Redis 6.x | 验证码存储、数据缓存 |

## 项目结构

```
campus-workspace/
├── campus-backend/              # SpringBoot 后端服务
│   ├── src/main/java/com/campus/
│   │   ├── annotation/          # 自定义注解（@NoAuth, @RoleRequired）
│   │   ├── common/              # 通用组件（Result, JwtUtil, 拦截器, 异常处理, 自动填充）
│   │   ├── config/              # 配置类（CORS, JWT, MyBatis-Plus, Redis）
│   │   ├── controller/          # API控制器（Auth/公告/失物/报修/文件/天气/统计/配置）
│   │   ├── dto/                 # 数据传输对象
│   │   ├── entity/              # 数据实体（User/Announcement/LostFound/RepairOrder等7个）
│   │   ├── enums/               # 枚举类（UserRole/UserStatus/RepairOrderStatus等5个）
│   │   ├── mapper/              # MyBatis-Plus数据访问层
│   │   └── service/             # 业务逻辑层（接口+实现）
│   └── src/main/resources/
│       ├── application.yml      # 主配置文件
│       └── mapper/              # XML映射文件
├── campus-admin/                # Vue 3 管理后台
│   └── src/
│       ├── views/               # 页面组件（登录/仪表盘/公告/失物/工单/用户/统计/配置/日志）
│       ├── router/              # 路由配置
│       ├── api/                 # API请求封装
│       └── components/          # 通用组件（CaptchaSlider滑块验证码）
├── campus-miniapp/              # 微信小程序
│   ├── pages/
│   │   ├── index/               # 首页（公告轮播+天气卡片+快捷入口）
│   │   ├── lostfound/           # 失物招领（列表+发布+详情+搜索）
│   │   ├── repair/              # 报修中心（提交+记录查询）
│   │   └── profile/             # 个人中心（登录+信息完善）
│   └── utils/                   # 工具函数（request请求封装）
└── docs/                        # 项目文档
    ├── database/                # 数据库设计文档
    │   ├── schema.sql           # 完整建表SQL（7张表+22个索引）
    │   ├── test_data.sql        # 测试数据
    │   ├── data_dictionary.md   # 数据字典
    │   └── er_diagram.svg       # E-R图
    ├── 项目开发报告-学号-姓名.docx  # 完整项目开发报告（含6章）
    └── 毕业实习报告-学号-姓名.docx  # 毕业实习报告
```

## 功能清单

### 微信小程序（学生端）

| 模块 | 功能 |
|------|------|
| 🏠 **首页** | 校园公告轮播、实时天气卡片、快捷功能入口、公告分类列表 |
| 🔍 **失物招领** | 失物/寻物信息浏览、发布信息、搜索筛选、信息详情 |
| 🔧 **报修中心** | 在线提交报修申请、报修记录列表、进度追踪（待处理→处理中→已完成） |
| 👤 **个人中心** | 用户信息、我的发布、我的报修、系统设置 |

### Vue管理后台（管理员端）

| 模块 | 功能 |
|------|------|
| 📊 **仪表盘** | 系统运营数据概览（公告数/工单数/用户数） |
| 📋 **公告管理** | 公告发布/编辑/删除、分类管理 |
| 🔎 **失物招领管理** | 信息审核（通过/驳回）、已发布管理 |
| 🛠️ **报修工单管理** | 工单受理/派单/完成/驳回、状态追踪 |
| 👥 **用户管理** | 用户列表、状态管理 |
| 📈 **数据统计** | 公告统计、工单统计图表 |

### 后端服务

| 接口分类 | 功能 |
|----------|------|
| 🔐 **鉴权** | 微信小程序登录、管理员登录、JWT Token签发与校验、滑块验证码 |
| 📂 **文件上传** | 单/多图上传、格式与大小校验 |
| 🌤️ **天气API** | 实时天气查询、天气预报、接口缓存 |
| 🔄 **工单流转** | 创建→受理→处理中→完成/驳回，全状态机管理 |
| 📋 **业务CRUD** | 公告/失物/用户/统计/系统配置增删改查 |
| 📝 **操作日志** | 系统操作审计追溯 |

## 快速启动

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.x+
- 微信开发者工具
- Maven 3.8+

### 启动步骤

**1. 数据库初始化**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 导入建表脚本和测试数据
mysql -u root -p campus < docs/database/schema.sql
mysql -u root -p campus < docs/database/test_data.sql
```

**2. 启动Redis**
```bash
# 默认端口6379，无需密码
redis-server
```

**3. 启动后端**
```bash
cd campus-backend
# 确保 application.yml 中数据库和Redis配置正确
# 本地开发默认使用 root/123456 + 本地Redis
.\mvnw.cmd -f pom.xml spring-boot:run
```

**4. 启动管理后台**
```bash
cd campus-admin
npm install
npm run dev
```
访问 http://localhost:3000

**5. 运行小程序**
- 打开微信开发者工具
- 导入 `campus-miniapp` 目录
- 编译运行

## 项目文档

项目配套文档位于 `docs/` 目录：

| 文档 | 说明 |
|------|------|
| [项目开发报告](docs/项目开发报告-学号-姓名.docx) | 完整项目开发文档（可行性分析、需求分析、概要设计、数据库设计、详细设计与实现、系统测试） |
| [毕业实习报告](docs/毕业实习报告-学号-姓名.docx) | 毕业实习报告模板 |
| [数据库设计文档](docs/database/) | 数据库建表SQL、测试数据、数据字典、E-R图 |
| [数据字典](docs/database/data_dictionary.md) | 7张表字段清单、索引方案、逻辑外键关系 |
| [E-R图](docs/database/er_diagram.svg) | 实体关系图 |

## 开发计划

| 阶段 | 时间 | 内容 |
|------|------|------|
| 需求分析 | Day 1-2 | 可行性分析、需求分析报告 |
| 概要设计 | Day 3-4 | 架构设计、数据库设计、接口设计 |
| 后端开发 | Day 5-8 | API开发、业务逻辑实现 |
| 前端开发 | Day 9-12 | 小程序+管理后台页面开发 |
| 联调测试 | Day 13 | 前后端联调、集成测试 |
| 部署交付 | Day 14 | 部署配置、文档整理 |

## 创新拓展方向

- 🏃 **校园跑腿**：代取快递、代买物品等跑腿服务
- 📬 **消息推送**：微信订阅消息，工单状态实时通知
- ⏰ **逾期提醒**：超时未处理工单自动提醒
- 🗺️ **校园地图**：集成地图导航功能

## 许可证

本项目为实训项目，仅用于教学目的。