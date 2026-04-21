# 视频平台项目

一个基于 Spring Boot + Vue.js 的在线视频平台系统，支持电影和剧集的在线播放、评论、收藏等功能。

## 项目简介

本平台提供以下核心功能：
- 📺 **视频播放**：支持电影和剧集在线播放
- 🎬 **智能封面**：自动从 TMDB/OMDB/TVMaze 获取并缓存封面
- 💬 **评论互动**：用户可对视频进行评分和评论
- ⭐ **收藏功能**：收藏喜欢的视频，记录观看进度
- 📝 **请求添加**：用户可请求添加新视频
- 🔧 **后台管理**：管理员可管理视频、处理请求、查看反馈

## 技术栈

### 后端
- **Spring Boot 4.0.1** - 核心框架
- **Spring Data JPA** - 数据持久化
- **MySQL** - 数据库
- **Lombok** - 简化代码
- **RestTemplate** - HTTP 客户端

### 前端
- **Vue.js 3** - 前端框架
- **DPlayer** - 视频播放器
- **Mermaid** - 流程图渲染

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 数据库配置

1. 创建数据库
```sql
CREATE DATABASE video_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置数据源（在 `src/main/resources/application.properties` 中配置）
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/video_platform
spring.datasource.username=root
spring.datasource.password=your_password
```

### 启动项目

```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd video-platform

# 启动项目
./mvnw spring-boot:run
```

访问：`http://localhost:8080`

## 项目结构

```
video-platform/
├── src/
│   ├── main/
│   │   ├── java/com/example/videoplatform/
│   │   │   ├── controller/      # 控制器层
│   │   │   ├── service/         # 服务层
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── entity/          # 实体类
│   │   │   └── common/          # 公共类
│   │   └── resources/
│   │       ├── static/          # 静态资源
│   │       └── application.properties
│   └── test/
├── docs/                        # 项目文档
│   ├── 流程图.md
│   └── API文档.md
├── covers/                      # 封面图片存储
├── uploads/                     # 视频文件存储
├── pom.xml
└── README.md
```

## 核心功能说明

### 1. 封面自动获取与缓存

系统支持从多个 API 源自动获取视频封面，并下载到本地存储：
- **TMDB API** - 优先使用，支持中文
- **OMDB API** - 备用方案
- **TVMaze API** - 仅用于剧集

封面文件存储在 `covers/` 目录，通过 `/covers/{filename}` 访问。

### 2. 用户权限系统

| 角色 | 权限 |
|------|------|
| 游客 | 浏览视频、查看评论 |
| 注册用户 | 评论、收藏、请求添加视频 |
| 管理员 | 视频管理、用户管理、处理请求 |

### 3. 视频播放

- **电影**：直接播放单个视频
- **剧集**：支持多集选择，记录观看进度
- **播放源**：支持本地视频和外部网页（iframe）

### 4. 评论与积分

- 用户对视频评分（1-10分）
- 发表评论内容
- 每部影片最多获得 2 积分
- 管理员可审核评论

## API 接口

### 用户端接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/videos` | GET | 获取视频列表 |
| `/api/video/{id}` | GET | 获取视频详情 |
| `/api/comment` | POST | 发表评论 |
| `/api/comment/video/{id}` | GET | 获取视频评论 |
| `/api/interaction/collect` | POST | 收藏/取消收藏 |
| `/api/interaction/progress` | POST | 记录观看进度 |
| `/api/request/add` | POST | 请求添加视频 |
| `/api/user/register` | POST | 用户注册 |
| `/api/user/login` | POST | 用户登录 |

### 管理员接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/admin/video/list` | GET | 获取所有视频 |
| `/admin/video/add` | POST | 添加视频 |
| `/admin/video/update` | PUT | 更新视频 |
| `/admin/video/delete/{id}` | DELETE | 删除视频 |
| `/admin/video/batch-import` | POST | 批量导入 |
| `/admin/video/fetch-poster/{id}` | POST | 获取单个封面 |
| `/admin/video/download-all-covers` | POST | 下载所有封面 |
| `/admin/request/list` | GET | 获取请求列表 |
| `/admin/feedback/list` | GET | 获取反馈列表 |

详细 API 文档请查看：[API文档.md](docs/API文档.md)

## 开发指南

### 添加新的视频源

1. 在 `PosterService.java` 中添加新的 API 调用方法
2. 在 `fetchPosterFromDouban()` 方法中添加调用逻辑

### 自定义封面存储位置

修改 `CoverImageService.java` 中的 `COVERS_DIR` 常量。

### 添加新的视频类型

1. 修改 `Video.java` 实体类
2. 更新前端页面逻辑
3. 调整数据库表结构

## 文档

- [业务流程图](docs/流程图.md) - 系统核心业务流程
- [API接口文档](docs/API文档.md) - 完整的API接口说明

## 常见问题

### Q: 封面图片加载失败？
A: 检查 `covers/` 目录是否存在且有写入权限，或查看控制台错误日志。

### Q: 视频无法播放？
A: 确认视频文件格式正确，播放地址配置正确。

### Q: 如何批量导入视频？
A: 在管理后台点击「批量导入」，上传符合格式的 JSON 文件。

## 更新日志

### v1.0.0 (2026-04-20)
- 初始版本发布
- 支持视频播放、评论、收藏功能
- 实现封面自动获取与本地缓存
- 完成用户请求添加功能
- 添加后台管理系统

## 许可证

MIT License

## 联系方式

如有问题或建议，欢迎提交 Issue。
