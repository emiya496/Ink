-- InkForge 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS inkforge DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE inkforge;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '绑定邮箱',
  `role` ENUM('user','admin') NOT NULL DEFAULT 'user' COMMENT '角色',
  `status` ENUM('正常','禁用') NOT NULL DEFAULT '正常' COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 内容表
CREATE TABLE IF NOT EXISTS `content` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '作者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` LONGTEXT COMMENT '正文(非小说用)',
  `type` ENUM('小说','散文','诗词','随笔','名人名言','杂谈') NOT NULL COMMENT '类型',
  `status` ENUM('正常','审核中','下架','草稿') NOT NULL DEFAULT '正常' COMMENT '状态',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '发布时间',
  `update_time` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW() COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容表';

-- 内容访问日志表（用于周阅读量统计）
CREATE TABLE IF NOT EXISTS `content_view_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `view_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '访问时间',
  INDEX `idx_content_id` (`content_id`),
  INDEX `idx_view_time` (`view_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容访问日志表';

-- 章节表（仅小说使用）
CREATE TABLE IF NOT EXISTS `chapter` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `content_id` BIGINT NOT NULL COMMENT '所属内容ID',
  `chapter_title` VARCHAR(200) DEFAULT NULL COMMENT '章节标题',
  `chapter_order` INT NOT NULL COMMENT '章节序号',
  `chapter_content` LONGTEXT COMMENT '章节内容',
  `create_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '创建时间',
  INDEX `idx_content_id` (`content_id`),
  INDEX `idx_order` (`content_id`, `chapter_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节表';

-- 标签表
CREATE TABLE IF NOT EXISTS `tag` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名',
  `type` ENUM('system','custom') NOT NULL DEFAULT 'system' COMMENT '类型',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人(自定义标签时有值)',
  `status` ENUM('正常','禁用') NOT NULL DEFAULT '正常' COMMENT '状态',
  `create_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '创建时间',
  UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 内容标签关联表
CREATE TABLE IF NOT EXISTS `content_tag` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  UNIQUE KEY `uk_content_tag` (`content_id`, `tag_id`),
  INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容标签关联表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `user_id` BIGINT NOT NULL COMMENT '评论者ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `create_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '评论时间',
  INDEX `idx_content_id` (`content_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `create_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '收藏时间',
  UNIQUE KEY `uk_user_content` (`user_id`, `content_id`),
  INDEX `idx_content_id` (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 点赞表
CREATE TABLE IF NOT EXISTS `user_like` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `create_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '点赞时间',
  UNIQUE KEY `uk_user_like` (`user_id`, `content_id`),
  INDEX `idx_like_content_id` (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- AI调用日志表
CREATE TABLE IF NOT EXISTS `ai_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `function_type` VARCHAR(50) NOT NULL COMMENT '功能类型: generate/summary/polish/keywords/sentiment/style/qa',
  `model_name` VARCHAR(100) NOT NULL COMMENT '使用的模型名称',
  `create_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '调用时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_function_type` (`function_type`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用日志表';

-- 邮箱验证码表
CREATE TABLE IF NOT EXISTS `email_code` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `email` VARCHAR(100) NOT NULL COMMENT '目标邮箱',
  `code` VARCHAR(10) NOT NULL COMMENT '验证码',
  `purpose` VARCHAR(20) NOT NULL DEFAULT 'bind' COMMENT '用途: bind/change',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `create_time` DATETIME NOT NULL DEFAULT NOW() COMMENT '创建时间',
  INDEX `idx_email_purpose` (`email`, `purpose`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮箱验证码表';

-- 初始化系统标签
INSERT INTO `tag` (`tag_name`, `type`, `status`) VALUES
('古风', 'system', '正常'),
('青春', 'system', '正常'),
('悬疑', 'system', '正常'),
('治愈', 'system', '正常'),
('校园', 'system', '正常'),
('言情', 'system', '正常'),
('武侠', 'system', '正常'),
('科幻', 'system', '正常'),
('历史', 'system', '正常'),
('都市', 'system', '正常'),
('奇幻', 'system', '正常'),
('温情', 'system', '正常'),
('哲思', 'system', '正常'),
('自然', 'system', '正常'),
('旅行', 'system', '正常'),
('成长', 'system', '正常');

-- 管理员账号由应用启动时 DataInitializer 自动创建并设置正确密码
-- 用户名: admin  密码: admin123
-- 此处不插入，避免 BCrypt hash 错误导致无法登录
-- 注意：如上hash不正确，可通过应用启动时DataInitializer自动初始化

-- ============================================================
-- 增量迁移（仅对已存在的数据库执行，全新安装无需执行）
-- ============================================================

-- 1. user 表新增 email 列（若列已存在会报错，可忽略）
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `email` VARCHAR(100) DEFAULT NULL COMMENT '绑定邮箱' AFTER `avatar`;
