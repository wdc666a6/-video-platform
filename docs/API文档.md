# 视频平台 API 接口文档

本文档描述了视频平台的所有 API 接口。

## 基础信息

- **基础URL**: `http://localhost:8080`
- **数据格式**: JSON
- **字符编码**: UTF-8

## 通用说明

### 响应格式

所有接口返回统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 一、用户相关接口

### 1.1 用户注册

**接口地址**: `POST /api/user/register`

**请求参数**:
```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

### 1.2 用户登录

**接口地址**: `POST /api/user/login`

**请求参数**:
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "preferences": "科幻,动作"
  }
}
```

### 1.3 获取用户信息

**接口地址**: `GET /api/user/info`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "points": 100,
    "preferences": "科幻,动作,大陆"
  }
}
```

### 1.4 更新用户偏好

**接口地址**: `PUT /api/user/preferences`

**请求参数**:
```json
{
  "preferences": "科幻,动作,大陆,美国"
}
```

---

## 二、视频相关接口

### 2.1 获取视频列表（分页）

**接口地址**: `GET /api/videos`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 搜索关键词 |
| type | String | 否 | 类型：MOVIE/SERIES |
| category | String | 否 | 分类 |
| region | String | 否 | 地区 |
| year | Integer | 否 | 年份 |
| language | String | 否 | 语言 |
| sortField | String | 否 | 排序：score/id/year |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认12 |

**请求示例**:
```
GET /api/videos?type=MOVIE&region=大陆&year=2023&pageNum=1&pageSize=12
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "title": "流浪地球2",
        "type": "MOVIE",
        "coverUrl": "https://example.com/poster.jpg",
        "localCoverPath": "/covers/1.jpg",
        "year": 2023,
        "region": "大陆",
        "category": "科幻,冒险",
        "score": 8.5,
        "description": "太阳即将毁灭..."
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 12
  }
}
```

### 2.2 获取视频详情

**接口地址**: `GET /api/video/{id}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "流浪地球2",
    "type": "MOVIE",
    "coverUrl": "https://example.com/poster.jpg",
    "localCoverPath": "/covers/1.jpg",
    "playUrl": "/uploads/video.mp4",
    "playSourceType": 0,
    "year": 2023,
    "region": "大陆",
    "language": "汉语普通话",
    "category": "科幻,冒险",
    "score": 8.5,
    "description": "太阳即将毁灭...",
    "episodes": null
  }
}
```

### 2.3 获取推荐视频

**接口地址**: `GET /api/videos/recommend`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2,
      "title": "三体",
      "type": "SERIES",
      "coverUrl": "...",
      "localCoverPath": "/covers/2.jpg",
      "score": 9.0
    }
  ]
}
```

---

## 三、评论相关接口

### 3.1 发表评论

**接口地址**: `POST /api/comment`

**请求参数**:
```json
{
  "videoId": 1,
  "content": "非常好看！",
  "rating": 9
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "评论成功，积分+2",
  "data": {
    "id": 1,
    "videoId": 1,
    "userId": 1,
    "content": "非常好看！",
    "rating": 9,
    "createTime": "2026-04-20T10:00:00"
  }
}
```

### 3.2 获取视频评论

**接口地址**: `GET /api/comment/video/{videoId}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "videoId": 1,
      "userId": 1,
      "username": "testuser",
      "content": "非常好看！",
      "rating": 9,
      "createTime": "2026-04-20T10:00:00"
    }
  ]
}
```

### 3.3 删除评论

**接口地址**: `DELETE /api/comment/{id}`

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 四、交互相关接口

### 4.1 收藏/取消收藏

**接口地址**: `POST /api/interaction/collect`

**请求参数**:
```json
{
  "videoId": 1,
  "collect": true
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "收藏成功"
}
```

### 4.2 记录观看进度

**接口地址**: `POST /api/interaction/progress`

**请求参数**:
```json
{
  "videoId": 1,
  "episodeIndex": 0,
  "progress": 120,
  "duration": 5400
}
```

| 参数 | 说明 |
|------|------|
| videoId | 视频ID |
| episodeIndex | 集数索引（剧集专用） |
| progress | 观看进度（秒） |
| duration | 视频总时长（秒） |

### 4.3 获取用户收藏

**接口地址**: `GET /api/interaction/collected`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "流浪地球2",
      "coverUrl": "...",
      "localCoverPath": "/covers/1.jpg",
      "score": 8.5
    }
  ]
}
```

### 4.4 获取观看历史

**接口地址**: `GET /api/interaction/history`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "videoId": 1,
      "episodeIndex": 0,
      "progress": 120,
      "lastWatchTime": "2026-04-20T15:30:00",
      "video": {
        "id": 1,
        "title": "流浪地球2",
        "coverUrl": "...",
        "localCoverPath": "/covers/1.jpg"
      }
    }
  ]
}
```

---

## 五、请求相关接口

### 5.1 添加请求

**接口地址**: `POST /api/request/add`

**请求参数**:
```json
{
  "title": "星际穿越",
  "note": "希望能添加这部电影"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "请求提交成功",
  "data": {
    "id": 1,
    "title": "星际穿越",
    "status": "pending",
    "createTime": "2026-04-20T10:00:00"
  }
}
```

### 5.2 获取我的请求

**接口地址**: `GET /api/request/my`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "星际穿越",
      "status": "pending",
      "note": "希望能添加这部电影",
      "createTime": "2026-04-20T10:00:00"
    }
  ]
}
```

---

## 六、管理员接口

### 6.1 视频管理

#### 6.1.1 获取所有视频

**接口地址**: `GET /admin/video/list`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "流浪地球2",
      "type": "MOVIE",
      "coverUrl": "...",
      "localCoverPath": "/covers/1.jpg",
      "year": 2023,
      "score": 8.5
    }
  ]
}
```

#### 6.1.2 添加视频

**接口地址**: `POST /admin/video/add`

**请求参数**:
```json
{
  "title": "新视频",
  "type": "MOVIE",
  "year": 2023,
  "region": "大陆",
  "language": "汉语普通话",
  "category": "科幻",
  "score": 8.0,
  "description": "视频简介",
  "playUrl": "/uploads/video.mp4",
  "playSourceType": 0,
  "coverUrl": ""
}
```

#### 6.1.3 更新视频

**接口地址**: `PUT /admin/video/update`

**请求参数**:
```json
{
  "id": 1,
  "title": "更新后的标题"
}
```

#### 6.1.4 删除视频

**接口地址**: `DELETE /admin/video/delete/{id}`

#### 6.1.5 批量导入

**接口地址**: `POST /admin/video/batch-import`

**请求参数**:
```json
[
  {
    "title": "视频1",
    "type": "MOVIE",
    "year": 2023
  },
  {
    "title": "视频2",
    "type": "SERIES"
  }
]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 2,
    "added": 2,
    "updated": 0,
    "failed": 0,
    "posterFetched": 2,
    "coverDownloaded": 2,
    "errors": []
  }
}
```

#### 6.1.6 获取单个封面

**接口地址**: `POST /admin/video/fetch-poster/{id}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "coverUrl": "https://example.com/poster.jpg",
    "localCoverPath": "/covers/1.jpg",
    "oldCoverUrl": "",
    "message": "获取成功"
  }
}
```

#### 6.1.7 下载所有封面

**接口地址**: `POST /admin/video/download-all-covers`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "skipped": 50,
    "successCount": 45,
    "failCount": 5,
    "failedTitles": ["视频A", "视频B"],
    "message": "下载完成: 成功 45 个, 跳过 50 个, 失败 5 个"
  }
}
```

### 6.2 请求管理

#### 6.2.1 获取请求列表

**接口地址**: `GET /admin/request/list`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "星际穿越",
      "userId": 1,
      "username": "testuser",
      "status": "pending",
      "createTime": "2026-04-20T10:00:00"
    }
  ]
}
```

#### 6.2.2 处理请求

**接口地址**: `PUT /admin/request/{id}/status`

**请求参数**:
```json
{
  "status": "completed"
}
```

状态值：`pending`（待处理）、`completed`（已完成）、`ignored`（已忽略）

### 6.3 反馈管理

#### 6.3.1 获取反馈列表

**接口地址**: `GET /admin/feedback/list`

#### 6.3.2 处理反馈

**接口地址**: `PUT /admin/feedback/{id}/status`

---

## 七、数据模型

### 7.1 Video（视频）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| title | String | 标题 |
| type | String | 类型：MOVIE/SERIES |
| category | String | 分类，逗号分隔 |
| playSourceType | Integer | 播放源类型：0-本地/直链，1-外部网页 |
| playUrl | String | 播放地址 |
| description | String | 简介 |
| coverUrl | String | 外部封面URL |
| localCoverPath | String | 本地封面路径 |
| year | Integer | 年份 |
| region | String | 地区 |
| language | String | 语言 |
| score | Double | 评分 |
| episodes | String | 集数信息（JSON） |

### 7.2 User（用户）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| username | String | 用户名 |
| password | String | 密码（加密） |
| email | String | 邮箱 |
| points | Integer | 积分 |
| preferences | String | 偏好标签，逗号分隔 |
| isAdmin | Boolean | 是否管理员 |

### 7.3 Comment（评论）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| videoId | Long | 视频ID |
| userId | Long | 用户ID |
| content | String | 评论内容 |
| rating | Integer | 评分（1-10） |
| createTime | DateTime | 创建时间 |

### 7.4 Interaction（交互）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| userId | Long | 用户ID |
| videoId | Long | 视频ID |
| isCollected | Boolean | 是否收藏 |
| watchProgress | Integer | 观看进度（秒） |
| lastWatchTime | DateTime | 最后观看时间 |

### 7.5 Request（请求）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| userId | Long | 用户ID |
| title | String | 请求的影片名称 |
| note | String | 备注 |
| status | String | 状态：pending/completed/ignored |
| createTime | DateTime | 创建时间 |

---

## 八、错误码说明

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户名已存在 |
| 1002 | 用户名或密码错误 |
| 1003 | 未登录 |
| 1004 | 无权限 |
| 2001 | 视频不存在 |
| 2002 | 视频已存在 |
| 3001 | 评论不能为空 |
| 3002 | 已超过评论积分限制 |
| 4001 | 请求已存在 |
| 5001 | 服务器内部错误 |

---

*文档更新时间: 2026-04-20*
