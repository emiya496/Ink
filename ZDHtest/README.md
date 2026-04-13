# ZDHtest

`ZDHtest` 现在不是“一个大测试文件”，而是一套按模块拆分、基于 `pytest` 封装出来的 InkForge 自动化测试工程：

- 接口自动化：`pytest + requests`
- UI 自动化：`pytest + selenium`
- HTML 报告：`pytest-html`
- 结构化封装：`core / api / utils / tests / pages`

## 目录结构

```text
ZDHtest/
├─ api/                # 按业务模块封装接口调用
├─ core/               # 通用能力：ApiClient、BasePage
├─ utils/              # 断言、测试数据工厂
├─ tests/
│  ├─ api/             # 接口测试用例
│  └─ ui/              # UI 测试用例
├─ conftest.py         # 全局夹具、浏览器驱动、报告截图
├─ run_tests.py        # 一键运行入口
├─ run_tests.bat       # Windows 双击入口
└─ pytest.ini
```

## 封装思路

这里用了我们刚刚聊的那套思路：

- `core/api_client.py`
  统一处理 `base_url`、`Authorization`、`Content-Type`、`get/post/put/delete`
- `api/*.py`
  把用户、内容、评论、章节、收藏、关注、标签、上传、AI、管理端接口拆成独立模块
- `tests/api/*.py`
  测试文件只写“测什么业务”，不再重复写请求细节
- `tests/ui/pages/*.py`
  UI 侧走 Page Object，把页面定位和操作从测试用例里抽出去

## 当前接口模块

`api/` 下已经拆成这些文件：

- `user_api.py`
- `content_api.py`
- `comment_api.py`
- `chapter_api.py`
- `favorite_api.py`
- `follow_api.py`
- `tag_api.py`
- `upload_api.py`
- `ai_api.py`
- `admin_api.py`

所以现在不是“只有一个 api 文件”，而是“一个模块一个 api 封装，一个模块一个测试文件”。

## 接口测试覆盖

当前已经按后端控制器拆分成这些测试文件：

- `tests/api/test_user_api.py`
- `tests/api/test_content_api.py`
- `tests/api/test_comment_api.py`
- `tests/api/test_chapter_api.py`
- `tests/api/test_favorite_api.py`
- `tests/api/test_follow_api.py`
- `tests/api/test_tag_api.py`
- `tests/api/test_upload_api.py`
- `tests/api/test_ai_api.py`
- `tests/api/test_admin_api.py`

覆盖范围包括：

- 用户：注册、登录、信息、主页、搜索、头像、简介、用户名、密码、邮箱相关、注销相关
- 内容：创建、列表、排行、轮播、详情、修改、删除、我的内容、草稿、我的点赞、他人作品、点赞切换
- 评论：新增、列表、删除
- 章节：新增、列表、修改、删除
- 收藏：添加、检查、列表、取消
- 关注：关注、取消关注、移除粉丝、关系检查、粉丝列表、关注列表
- 标签：列表、创建、热门标签
- 上传：图片上传
- AI：生成、总结、润色、关键词、情感、风格分析、问答
- 管理端：用户、内容、标签、评论、AI 统计相关接口

说明：

- 邮箱绑定、改绑、重置密码、注销账号这类接口需要验证码，已经做成“可选测试”
- AI 接口需要你本地 AI 配置可用，默认也是可选测试

## UI 测试覆盖

UI 这边目前是“关键页面和关键链路的 smoke 测试”，不是每个按钮都做了全量自动化。

当前覆盖：

- 登录页真实登录
- 首页、分类页、详情页、用户主页等公开路由可正常打开
- 发布页、个人中心、书架等登录后路由可正常打开
- 管理端用户、内容、标签、评论、AI 统计页面可正常打开
- 失败时自动截图并挂到 HTML 报告里

## 运行前提

1. 先启动项目
   - 后端：`http://localhost:9090`
   - 前端：`http://localhost:5173`
2. 本机安装 Python 3.13+
3. 本机安装浏览器和对应驱动
   - 默认使用 Chrome
   - 也支持 Edge / Firefox

## 安装依赖

在 `ZDHtest` 目录执行：

```bash
pip install -r requirements.txt
```

## 常用环境变量

```text
INKFORGE_API_BASE_URL=http://localhost:9090
INKFORGE_UI_BASE_URL=http://localhost:5173
INKFORGE_ADMIN_USERNAME=admin
INKFORGE_ADMIN_PASSWORD=admin123
INKFORGE_BROWSER=chrome
INKFORGE_HEADLESS=true
INKFORGE_WAIT_TIMEOUT=20
INKFORGE_ENABLE_AI_TESTS=false
INKFORGE_UPLOAD_FILE=自定义上传文件路径
```

可选邮箱相关变量：

```text
INKFORGE_BIND_EMAIL=
INKFORGE_BIND_EMAIL_CODE=
INKFORGE_CHANGE_EMAIL=
INKFORGE_CHANGE_EMAIL_CODE=
INKFORGE_RESET_EMAIL=
INKFORGE_RESET_EMAIL_CODE=
INKFORGE_DELETE_EMAIL_CODE=
```

说明：

- `INKFORGE_HEADLESS=false` 可以看到浏览器真实执行过程
- `INKFORGE_ENABLE_AI_TESTS=true` 才会运行 AI 接口测试
- 邮箱验证码相关变量不填时，对应测试会自动跳过

## 运行方式

运行全部测试：

```bash
python run_tests.py
```

只跑接口测试：

```bash
python run_tests.py --suite api
```

只跑 UI 测试：

```bash
python run_tests.py --suite ui
```

Windows 下也可以直接双击：

```text
run_tests.bat
run_api_tests.bat
run_ui_tests.bat
```

## 报告输出

执行完成后会在 `reports/` 下生成：

- `report-all-时间戳.html`
- `report-api-时间戳.html`
- `report-ui-时间戳.html`
- `report-xxx-时间戳-zh.html`

UI 失败截图会保存到：

```text
reports/screenshots/
```

说明：

- 普通 `html` 是 `pytest-html` 原始报告
- `-zh.html` 是额外生成的中文摘要版报告，更适合直接查看失败原因

## 额外说明

- 大部分测试数据会在运行时自动创建，尽量不依赖固定账号
- 管理员账号默认走项目初始化的 `admin / admin123`
- 这套代码已经从“单文件脚本”升级成了“可继续扩展的自动化测试框架雏形”

后面如果你想继续往公司里常见的方向演进，可以再加：

- 参数化数据驱动
- Allure 报告
- `.env` 配置加载
- Jenkins / GitLab CI 集成
- 更细粒度的 UI 用例和业务链路断言
