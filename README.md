# InkForge — 启动指南

## 环境要求
- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0

## 1. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本
source /path/to/inkforge-backend/src/main/resources/db/init.sql
```

或直接在 MySQL Workbench / Navicat 中执行 `init.sql`。

**初始账号**（应用首次启动时自动创建）：
- 管理员：`admin` / `admin123`

## 2. 配置数据库连接

编辑 `inkforge-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/inkforge?...
    username: root
    password: your_password  # 修改为你的数据库密码
```

## 3. 配置 AI API Key

```yaml
ai:
  provider: deepseek          # 可选: deepseek / openai / claude
  deepseek:
    api-key: sk-xxxx          # 填入 DeepSeek API Key
```

或通过环境变量：
```bash
export DEEPSEEK_API_KEY=sk-xxxx
```

## 4. 启动后端

```bash
cd inkforge-backend
mvn spring-boot:run
# 后端启动在 http://localhost:8080
```

## 5. 启动前端

```bash
cd inkforge-frontend
npm run dev
# 前端启动在 http://localhost:5173
```

## 6. 访问系统

- **前台首页**：http://localhost:5173
- **登录/注册**：http://localhost:5173/login
- **管理后台**：http://localhost:5173/admin（需管理员账号）

## 功能清单

### 普通用户
- [x] 注册/登录
- [x] 首页内容浏览（热门/最新）
- [x] 分类浏览 + 搜索
- [x] 发布内容（小说/散文/诗词/随笔/名人名言/杂谈）
- [x] 小说章节管理
- [x] 标签选择（系统标签 + 自定义标签，最多5个）
- [x] AI写作助手（续写 + 润色）
- [x] 阅读页（正文 + 标签 + 评论 + 收藏 + 点赞）
- [x] AI阅读助手（摘要/关键词/情感分析/文风分析/AI问答）
- [x] 个人中心
- [x] 我的书架（收藏列表）

### 管理员
- [x] 用户管理（查看/禁用/删除）
- [x] 内容管理（查看/下架/删除）
- [x] 标签管理（新增系统标签/禁用/删除）
- [x] 评论管理（删除违规评论）
- [x] AI调用统计（总次数/按用户/按功能）

## API 接口列表

| 模块 | 接口 |
|------|------|
| 用户 | POST /api/user/register, POST /api/user/login, GET /api/user/info |
| 内容 | POST /api/content/create, GET /api/content/list, GET /api/content/{id} |
| 章节 | POST /api/chapter, GET /api/chapter/{contentId} |
| 标签 | GET /api/tag/list, POST /api/tag/create, GET /api/tag/hot |
| 评论 | POST /api/comment, GET /api/comment/{contentId} |
| 收藏 | POST /api/favorite/{id}, DELETE /api/favorite/{id} |
| AI | POST /api/ai/generate, /summary, /polish, /keywords, /sentiment, /style, /qa |
| 管理 | GET /api/admin/users, /contents, /tags, /comments, /ai/stats |
