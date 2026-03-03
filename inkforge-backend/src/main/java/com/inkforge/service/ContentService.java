package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkforge.common.PageResult;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.dto.request.ContentCreateRequest;
import com.inkforge.dto.response.ContentVO;
import com.inkforge.dto.response.TagVO;
import com.inkforge.entity.*;
import com.inkforge.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContentService {

    private final ContentMapper contentMapper;
    private final UserMapper userMapper;
    private final TagService tagService;
    private final ContentTagMapper contentTagMapper;
    private final CommentMapper commentMapper;
    private final FavoriteMapper favoriteMapper;
    private final ContentViewLogMapper contentViewLogMapper;
    private final UserLikeMapper userLikeMapper;

    public ContentService(ContentMapper contentMapper, UserMapper userMapper,
                          TagService tagService, ContentTagMapper contentTagMapper,
                          CommentMapper commentMapper, FavoriteMapper favoriteMapper,
                          ContentViewLogMapper contentViewLogMapper,
                          UserLikeMapper userLikeMapper) {
        this.contentMapper = contentMapper;
        this.userMapper = userMapper;
        this.tagService = tagService;
        this.contentTagMapper = contentTagMapper;
        this.commentMapper = commentMapper;
        this.favoriteMapper = favoriteMapper;
        this.contentViewLogMapper = contentViewLogMapper;
        this.userLikeMapper = userLikeMapper;
    }

    @Transactional
    public Long create(ContentCreateRequest req, Long userId) {
        boolean isDraft = Boolean.TRUE.equals(req.getIsDraft());
        // 非草稿时校验必填字段
        if (!isDraft) {
            if (req.getTitle() == null || req.getTitle().isBlank()) throw new BusinessException(400, "标题不能为空");
            if (req.getType() == null || req.getType().isBlank()) throw new BusinessException(400, "请选择内容类型");
        }
        Content content = new Content();
        content.setUserId(userId);
        content.setTitle(req.getTitle() != null ? req.getTitle() : "未命名草稿");
        content.setContent(req.getContent());
        content.setType(req.getType() != null ? req.getType() : "散文");
        content.setStatus(isDraft ? "草稿" : "正常");
        content.setCoverImage(req.getCoverImage());
        contentMapper.insert(content);

        // 处理标签（去重，限5个）
        bindTags(content.getId(), req.getTagIds(), req.getCustomTags(), userId);
        return content.getId();
    }

    @Transactional
    public void update(Long contentId, ContentCreateRequest req, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) throw new BusinessException(404, "内容不存在");
        if (!content.getUserId().equals(userId)) throw new BusinessException(403, "无权修改");

        boolean isDraft = Boolean.TRUE.equals(req.getIsDraft());
        // 从草稿发布时校验必填字段
        if (!isDraft && "草稿".equals(content.getStatus())) {
            if (req.getTitle() == null || req.getTitle().isBlank()) throw new BusinessException(400, "标题不能为空");
            if (req.getType() == null || req.getType().isBlank()) throw new BusinessException(400, "请选择内容类型");
        }

        if (req.getTitle() != null) content.setTitle(req.getTitle());
        if (req.getContent() != null) content.setContent(req.getContent());
        if (req.getType() != null) content.setType(req.getType());
        if (req.getCoverImage() != null) content.setCoverImage(req.getCoverImage());

        // 状态管理
        if (isDraft) {
            content.setStatus("草稿");
        } else if ("草稿".equals(content.getStatus())) {
            content.setStatus("正常"); // 草稿发布为正常
        }
        // 已发布内容修改时保持原状态不变

        contentMapper.updateById(content);

        // 重新绑定标签
        contentTagMapper.delete(new LambdaQueryWrapper<ContentTag>()
                .eq(ContentTag::getContentId, contentId));
        bindTags(contentId, req.getTagIds(), req.getCustomTags(), userId);
    }

    @Transactional
    public void delete(Long contentId, Long userId, String role) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) throw new BusinessException(404, "内容不存在");
        if (!"admin".equals(role) && !content.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除");
        }
        contentMapper.deleteById(contentId);
        contentTagMapper.delete(new LambdaQueryWrapper<ContentTag>()
                .eq(ContentTag::getContentId, contentId));
    }

    public PageResult<ContentVO> list(Integer page, Integer size, String type, String keyword,
                                      String sortBy, Long tagId, Long currentUserId) {
        String typeParam = (type == null) ? "" : type;
        String kwParam = (keyword == null) ? "" : keyword;
        long tagIdParam = (tagId == null) ? 0L : tagId;

        // 需要 JOIN 聚合的排序类型，走自定义分页查询
        if ("weeklyReads".equals(sortBy)) {
            long offset = (long) (page - 1) * size;
            List<Content> records = contentMapper.listByWeeklyReads(typeParam, kwParam, tagIdParam, offset, size);
            long total = contentMapper.countList(typeParam, kwParam, tagIdParam);
            List<ContentVO> voList = records.stream()
                    .map(c -> toVO(c, currentUserId, false)).collect(Collectors.toList());
            return PageResult.of(total, voList, page, size);
        }
        if ("favorites".equals(sortBy)) {
            long offset = (long) (page - 1) * size;
            List<Content> records = contentMapper.listByFavorites(typeParam, kwParam, tagIdParam, offset, size);
            long total = contentMapper.countList(typeParam, kwParam, tagIdParam);
            List<ContentVO> voList = records.stream()
                    .map(c -> toVO(c, currentUserId, false)).collect(Collectors.toList());
            return PageResult.of(total, voList, page, size);
        }

        // 普通字段排序，MyBatis-Plus 分页
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<Content>()
                .eq(Content::getStatus, "正常");
        if (!typeParam.isEmpty()) wrapper.eq(Content::getType, type);
        if (!kwParam.isEmpty()) wrapper.like(Content::getTitle, keyword);
        if (tagIdParam != 0L) {
            // 标签过滤：content_id 在 content_tag 中存在对应 tag_id
            List<ContentTag> cts = contentTagMapper.selectList(
                    new LambdaQueryWrapper<ContentTag>().eq(ContentTag::getTagId, tagIdParam));
            if (cts.isEmpty()) {
                return PageResult.of(0L, List.of(), page, size);
            }
            List<Long> contentIds = cts.stream().map(ContentTag::getContentId).collect(Collectors.toList());
            wrapper.in(Content::getId, contentIds);
        }

        switch (sortBy == null ? "" : sortBy) {
            case "viewCount" -> wrapper.orderByDesc(Content::getViewCount);
            case "likeCount" -> wrapper.orderByDesc(Content::getLikeCount);
            default         -> wrapper.orderByDesc(Content::getCreateTime);
        }

        IPage<Content> pageResult = contentMapper.selectPage(new Page<>(page, size), wrapper);
        List<ContentVO> voList = pageResult.getRecords().stream()
                .map(c -> toVO(c, currentUserId, false))
                .collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList, page, size);
    }

    public ContentVO getDetail(Long contentId, Long currentUserId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || "下架".equals(content.getStatus())) {
            throw new BusinessException(404, "内容不存在或已下架");
        }
        // 草稿不增加浏览量，不记录日志
        if (!"草稿".equals(content.getStatus())) {
            content.setViewCount(content.getViewCount() + 1);
            contentMapper.updateById(content);
            // 记录访问日志（用于周阅读量统计）
            ContentViewLog log = new ContentViewLog();
            log.setContentId(contentId);
            log.setViewTime(LocalDateTime.now());
            contentViewLogMapper.insert(log);
        }
        return toVO(content, currentUserId, true);
    }

    public PageResult<ContentVO> getMyContent(Long userId, Integer page, Integer size) {
        IPage<Content> pageResult = contentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Content>()
                        .eq(Content::getUserId, userId)
                        .ne(Content::getStatus, "草稿")   // 排除草稿
                        .orderByDesc(Content::getCreateTime));
        List<ContentVO> voList = pageResult.getRecords().stream()
                .map(c -> toVO(c, userId, false))
                .collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList, page, size);
    }

    public PageResult<ContentVO> getMyDrafts(Long userId, Integer page, Integer size) {
        IPage<Content> pageResult = contentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Content>()
                        .eq(Content::getUserId, userId)
                        .eq(Content::getStatus, "草稿")
                        .orderByDesc(Content::getCreateTime));
        List<ContentVO> voList = pageResult.getRecords().stream()
                .map(c -> toVO(c, userId, false))
                .collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList, page, size);
    }

    /** 点赞/取消点赞（切换），返回操作后是否已点赞 */
    @Transactional
    public boolean toggleLike(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) throw new BusinessException(404, "内容不存在");

        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getContentId, contentId);
        UserLike existing = userLikeMapper.selectOne(wrapper);

        if (existing != null) {
            // 已点赞 → 取消
            userLikeMapper.delete(wrapper);
            content.setLikeCount(Math.max(0, content.getLikeCount() - 1));
            contentMapper.updateById(content);
            return false;
        } else {
            // 未点赞 → 点赞
            UserLike like = new UserLike();
            like.setUserId(userId);
            like.setContentId(contentId);
            userLikeMapper.insert(like);
            content.setLikeCount(content.getLikeCount() + 1);
            contentMapper.updateById(content);
            return true;
        }
    }

    /** 获取排行榜 TOP10 */
    public List<ContentVO> getRanking(String type, String rankType) {
        String typeParam = (type == null) ? "" : type;
        List<Content> list;
        switch (rankType) {
            case "weeklyReads": list = contentMapper.rankByWeeklyReads(typeParam); break;
            case "likes":       list = contentMapper.rankByLikes(typeParam); break;
            case "favorites":   list = contentMapper.rankByFavorites(typeParam); break;
            default:            list = contentMapper.rankByReads(typeParam);
        }
        return list.stream().map(this::toSimpleVO).collect(Collectors.toList());
    }

    /** 获取首页热门横幅内容（近7天最火，无则降级） */
    public ContentVO getHotBanner(String type) {
        String typeParam = (type == null) ? "" : type;
        Content content = contentMapper.getHotBannerContent(typeParam);
        if (content == null) {
            List<Content> top = contentMapper.rankByReads(typeParam);
            if (top.isEmpty()) return null;
            content = top.get(0);
        }
        return toSimpleVO(content);
    }

    // ========== 私有辅助方法 ==========

    private void bindTags(Long contentId, List<Long> tagIds, List<String> customTags, Long userId) {
        Set<Long> finalTagIds = new java.util.HashSet<>();
        if (tagIds != null) {
            finalTagIds.addAll(tagIds);
        }
        if (customTags != null) {
            for (String name : customTags) {
                if (name != null && !name.isBlank()) {
                    Tag tag = tagService.createCustomTag(name.trim(), userId);
                    finalTagIds.add(tag.getId());
                }
            }
        }
        // 最多5个
        List<Long> limited = new ArrayList<>(finalTagIds).subList(0, Math.min(finalTagIds.size(), 5));
        for (Long tagId : limited) {
            Tag tag = tagService.getById(tagId);
            if (tag != null && "正常".equals(tag.getStatus())) {
                ContentTag ct = new ContentTag();
                ct.setContentId(contentId);
                ct.setTagId(tagId);
                contentTagMapper.insert(ct);
            }
        }
    }

    /** 完整 VO（含标签、评论数、收藏状态） */
    private ContentVO toVO(Content content, Long currentUserId, boolean withCommentCount) {
        ContentVO vo = new ContentVO();
        vo.setId(content.getId());
        vo.setUserId(content.getUserId());
        vo.setTitle(content.getTitle());
        vo.setContent(content.getContent());
        vo.setType(content.getType());
        vo.setStatus(content.getStatus());
        vo.setViewCount(content.getViewCount());
        vo.setLikeCount(content.getLikeCount());
        vo.setCoverImage(content.getCoverImage());
        vo.setCreateTime(content.getCreateTime());

        // 作者信息
        User author = userMapper.selectById(content.getUserId());
        if (author != null) {
            vo.setUsername(author.getUsername());
            vo.setAvatar(author.getAvatar());
        }

        // 标签
        List<Tag> tags = tagService.getTagsByContentId(content.getId());
        vo.setTags(tags.stream().map(t -> {
            TagVO tv = new TagVO();
            tv.setId(t.getId());
            tv.setTagName(t.getTagName());
            tv.setType(t.getType());
            tv.setStatus(t.getStatus());
            return tv;
        }).collect(Collectors.toList()));

        // 评论数
        if (withCommentCount) {
            Long commentCount = commentMapper.selectCount(
                    new LambdaQueryWrapper<Comment>().eq(Comment::getContentId, content.getId()));
            vo.setCommentCount(commentCount.intValue());
        }

        // 收藏状态 & 点赞状态
        if (currentUserId != null) {
            Long favCount = favoriteMapper.selectCount(
                    new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, currentUserId)
                            .eq(Favorite::getContentId, content.getId()));
            vo.setFavorited(favCount > 0);

            Long likeCount = userLikeMapper.selectCount(
                    new LambdaQueryWrapper<UserLike>()
                            .eq(UserLike::getUserId, currentUserId)
                            .eq(UserLike::getContentId, content.getId()));
            vo.setLiked(likeCount > 0);
        }

        return vo;
    }

    /** 轻量 VO（排行 / 横幅用，不含正文、评论数、收藏状态） */
    private ContentVO toSimpleVO(Content content) {
        ContentVO vo = new ContentVO();
        vo.setId(content.getId());
        vo.setTitle(content.getTitle());
        vo.setType(content.getType());
        vo.setViewCount(content.getViewCount());
        vo.setLikeCount(content.getLikeCount());
        vo.setCoverImage(content.getCoverImage());
        vo.setCreateTime(content.getCreateTime());

        // 收藏总数
        Long favCount = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getContentId, content.getId()));
        vo.setFavoriteCount(favCount.intValue());

        User author = userMapper.selectById(content.getUserId());
        if (author != null) {
            vo.setUserId(author.getId());
            vo.setUsername(author.getUsername());
            vo.setAvatar(author.getAvatar());
        }
        return vo;
    }
}
