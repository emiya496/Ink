package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkforge.common.PageResult;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.dto.request.ContentCreateRequest;
import com.inkforge.dto.response.ContentVO;
import com.inkforge.dto.response.TagVO;
import com.inkforge.entity.Comment;
import com.inkforge.entity.Content;
import com.inkforge.entity.ContentTag;
import com.inkforge.entity.ContentViewLog;
import com.inkforge.entity.Favorite;
import com.inkforge.entity.Tag;
import com.inkforge.entity.User;
import com.inkforge.entity.UserFollow;
import com.inkforge.entity.UserLike;
import com.inkforge.mapper.CommentMapper;
import com.inkforge.mapper.ContentMapper;
import com.inkforge.mapper.ContentTagMapper;
import com.inkforge.mapper.ContentViewLogMapper;
import com.inkforge.mapper.FavoriteMapper;
import com.inkforge.mapper.UserFollowMapper;
import com.inkforge.mapper.UserLikeMapper;
import com.inkforge.mapper.UserMapper;
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
    private final UserFollowMapper userFollowMapper;

    public ContentService(ContentMapper contentMapper,
                          UserMapper userMapper,
                          TagService tagService,
                          ContentTagMapper contentTagMapper,
                          CommentMapper commentMapper,
                          FavoriteMapper favoriteMapper,
                          ContentViewLogMapper contentViewLogMapper,
                          UserLikeMapper userLikeMapper,
                          UserFollowMapper userFollowMapper) {
        this.contentMapper = contentMapper;
        this.userMapper = userMapper;
        this.tagService = tagService;
        this.contentTagMapper = contentTagMapper;
        this.commentMapper = commentMapper;
        this.favoriteMapper = favoriteMapper;
        this.contentViewLogMapper = contentViewLogMapper;
        this.userLikeMapper = userLikeMapper;
        this.userFollowMapper = userFollowMapper;
    }

    @Transactional
    public Long create(ContentCreateRequest req, Long userId) {
        boolean isDraft = Boolean.TRUE.equals(req.getIsDraft());
        if (!isDraft) {
            if (req.getTitle() == null || req.getTitle().isBlank()) {
                throw new BusinessException(400, "标题不能为空");
            }
            if (req.getType() == null || req.getType().isBlank()) {
                throw new BusinessException(400, "请选择内容类型");
            }
        }

        Content content = new Content();
        content.setUserId(userId);
        content.setTitle(req.getTitle() != null ? req.getTitle() : "未命名草稿");
        content.setContent(req.getContent());
        content.setType(req.getType() != null ? req.getType() : "散文");
        content.setStatus(isDraft ? "草稿" : "正常");
        content.setCoverImage(req.getCoverImage());
        content.setVisibility(req.getVisibility() != null ? req.getVisibility() : "public");
        contentMapper.insert(content);

        bindTags(content.getId(), req.getTagIds(), req.getCustomTags(), userId);
        return content.getId();
    }

    @Transactional
    public void update(Long contentId, ContentCreateRequest req, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(404, "内容不存在");
        }
        if (!content.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改");
        }

        boolean isDraft = Boolean.TRUE.equals(req.getIsDraft());
        if (!isDraft && "草稿".equals(content.getStatus())) {
            if (req.getTitle() == null || req.getTitle().isBlank()) {
                throw new BusinessException(400, "标题不能为空");
            }
            if (req.getType() == null || req.getType().isBlank()) {
                throw new BusinessException(400, "请选择内容类型");
            }
        }

        if (req.getTitle() != null) {
            content.setTitle(req.getTitle());
        }
        if (req.getContent() != null) {
            content.setContent(req.getContent());
        }
        if (req.getType() != null) {
            content.setType(req.getType());
        }
        if (req.getCoverImage() != null) {
            content.setCoverImage(req.getCoverImage());
        }
        if (req.getVisibility() != null) {
            content.setVisibility(req.getVisibility());
        }

        if (isDraft) {
            content.setStatus("草稿");
        } else if ("草稿".equals(content.getStatus())) {
            content.setStatus("正常");
        }
        contentMapper.updateById(content);

        contentTagMapper.delete(new LambdaQueryWrapper<ContentTag>()
                .eq(ContentTag::getContentId, contentId));
        bindTags(contentId, req.getTagIds(), req.getCustomTags(), userId);
    }

    @Transactional
    public void delete(Long contentId, Long userId, String role) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(404, "内容不存在");
        }
        if (!"admin".equals(role) && !content.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除");
        }
        contentMapper.deleteById(contentId);
        contentTagMapper.delete(new LambdaQueryWrapper<ContentTag>()
                .eq(ContentTag::getContentId, contentId));
    }

    public PageResult<ContentVO> list(Integer page, Integer size, String type, String keyword,
                                      String sortBy, Long tagId, Long currentUserId) {
        String typeParam = type == null ? "" : type;
        String keywordParam = keyword == null ? "" : keyword;
        long tagIdParam = tagId == null ? 0L : tagId;

        if ("weeklyReads".equals(sortBy)) {
            long offset = (long) (page - 1) * size;
            List<Content> records = contentMapper.listByWeeklyReads(typeParam, keywordParam, tagIdParam, offset, size);
            long total = contentMapper.countList(typeParam, keywordParam, tagIdParam);
            List<ContentVO> voList = records.stream()
                    .map(content -> toVO(content, currentUserId, false))
                    .collect(Collectors.toList());
            return PageResult.of(total, voList, page, size);
        }
        if ("favorites".equals(sortBy)) {
            long offset = (long) (page - 1) * size;
            List<Content> records = contentMapper.listByFavorites(typeParam, keywordParam, tagIdParam, offset, size);
            long total = contentMapper.countList(typeParam, keywordParam, tagIdParam);
            List<ContentVO> voList = records.stream()
                    .map(content -> toVO(content, currentUserId, false))
                    .collect(Collectors.toList());
            return PageResult.of(total, voList, page, size);
        }

        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<Content>()
                .eq(Content::getStatus, "正常")
                .eq(Content::getVisibility, "public");
        if (!typeParam.isEmpty()) {
            wrapper.eq(Content::getType, typeParam);
        }
        if (!keywordParam.isEmpty()) {
            wrapper.like(Content::getTitle, keywordParam);
        }
        if (tagIdParam != 0L) {
            List<ContentTag> contentTags = contentTagMapper.selectList(
                    new LambdaQueryWrapper<ContentTag>().eq(ContentTag::getTagId, tagIdParam));
            if (contentTags.isEmpty()) {
                return PageResult.of(0L, List.of(), page, size);
            }
            List<Long> contentIds = contentTags.stream()
                    .map(ContentTag::getContentId)
                    .collect(Collectors.toList());
            wrapper.in(Content::getId, contentIds);
        }

        switch (sortBy == null ? "" : sortBy) {
            case "viewCount" -> wrapper.orderByDesc(Content::getViewCount);
            case "likeCount" -> wrapper.orderByDesc(Content::getLikeCount);
            default -> wrapper.orderByDesc(Content::getCreateTime);
        }

        IPage<Content> pageResult = contentMapper.selectPage(new Page<>(page, size), wrapper);
        List<ContentVO> voList = pageResult.getRecords().stream()
                .map(content -> toVO(content, currentUserId, false))
                .collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList, page, size);
    }

    public ContentVO getDetail(Long contentId, Long currentUserId) {
        Content content = contentMapper.selectById(contentId);
        String inaccessibleReason = getInaccessibleReason(content, currentUserId);
        if (inaccessibleReason != null) {
            throwInaccessibleException(inaccessibleReason);
        }

        if (!"草稿".equals(content.getStatus())) {
            content.setViewCount(content.getViewCount() + 1);
            contentMapper.updateById(content);

            ContentViewLog log = new ContentViewLog();
            log.setContentId(contentId);
            log.setViewTime(LocalDateTime.now());
            contentViewLogMapper.insert(log);
        }
        return toVO(content, currentUserId, true);
    }

    public ContentVO getAccessibleContentSnapshot(Content content, Long currentUserId) {
        String inaccessibleReason = getInaccessibleReason(content, currentUserId);
        if (inaccessibleReason != null) {
            throwInaccessibleException(inaccessibleReason);
        }
        return toVO(content, currentUserId, false);
    }

    public String getInaccessibleReason(Content content, Long currentUserId) {
        if (content == null) {
            return "deleted";
        }
        if ("下架".equals(content.getStatus())) {
            return "taken_down";
        }

        boolean isOwner = currentUserId != null && content.getUserId().equals(currentUserId);
        if ("草稿".equals(content.getStatus()) && !isOwner) {
            return "private";
        }

        String visibility = content.getVisibility();
        if ("private".equals(visibility) && !isOwner) {
            return "private";
        }
        if ("followers_only".equals(visibility) && !isOwner) {
            if (currentUserId == null) {
                return "followers_only";
            }
            boolean following = userFollowMapper.selectCount(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getFollowerId, currentUserId)
                            .eq(UserFollow::getFolloweeId, content.getUserId())) > 0;
            if (!following) {
                return "followers_only";
            }
        }

        return null;
    }

    public PageResult<ContentVO> getMyContent(Long userId, Integer page, Integer size) {
        IPage<Content> pageResult = contentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Content>()
                        .eq(Content::getUserId, userId)
                        .ne(Content::getStatus, "草稿")
                        .orderByDesc(Content::getCreateTime));
        List<ContentVO> voList = pageResult.getRecords().stream()
                .map(content -> toVO(content, userId, false))
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
                .map(content -> toVO(content, userId, false))
                .collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList, page, size);
    }

    public PageResult<ContentVO> getUserWorks(Long authorId, Long viewerId, Integer page, Integer size) {
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<Content>()
                .eq(Content::getUserId, authorId)
                .eq(Content::getStatus, "正常")
                .orderByDesc(Content::getCreateTime);

        if (viewerId == null || !viewerId.equals(authorId)) {
            boolean isFollowing = viewerId != null && userFollowMapper.selectCount(
                    new LambdaQueryWrapper<UserFollow>()
                            .eq(UserFollow::getFollowerId, viewerId)
                            .eq(UserFollow::getFolloweeId, authorId)) > 0;
            if (isFollowing) {
                wrapper.in(Content::getVisibility, List.of("public", "followers_only"));
            } else {
                wrapper.eq(Content::getVisibility, "public");
            }
        }

        IPage<Content> pageResult = contentMapper.selectPage(new Page<>(page, size), wrapper);
        List<ContentVO> voList = pageResult.getRecords().stream()
                .map(content -> toVO(content, viewerId, false))
                .collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList, page, size);
    }

    public PageResult<ContentVO> getMyLikes(Long userId, Integer page, Integer size) {
        IPage<UserLike> likePage = userLikeMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserLike>()
                        .eq(UserLike::getUserId, userId)
                        .orderByDesc(UserLike::getCreateTime));
        List<Long> contentIds = likePage.getRecords().stream()
                .map(UserLike::getContentId)
                .collect(Collectors.toList());
        if (contentIds.isEmpty()) {
            return PageResult.of(likePage.getTotal(), List.of(), page, size);
        }
        List<Content> contents = contentMapper.selectBatchIds(contentIds);
        List<ContentVO> voList = contents.stream()
                .filter(content -> "正常".equals(content.getStatus()))
                .map(content -> toVO(content, userId, false))
                .collect(Collectors.toList());
        return PageResult.of(likePage.getTotal(), voList, page, size);
    }

    @Transactional
    public boolean toggleLike(Long contentId, Long userId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(404, "内容不存在");
        }

        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getContentId, contentId);
        UserLike existing = userLikeMapper.selectOne(wrapper);

        if (existing != null) {
            userLikeMapper.delete(wrapper);
            content.setLikeCount(Math.max(0, content.getLikeCount() - 1));
            contentMapper.updateById(content);
            return false;
        }

        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setContentId(contentId);
        userLikeMapper.insert(like);
        content.setLikeCount(content.getLikeCount() + 1);
        contentMapper.updateById(content);
        return true;
    }

    public List<ContentVO> getRanking(String type, String rankType) {
        String typeParam = type == null ? "" : type;
        List<Content> list;
        switch (rankType) {
            case "weeklyReads" -> list = contentMapper.rankByWeeklyReads(typeParam);
            case "likes" -> list = contentMapper.rankByLikes(typeParam);
            case "favorites" -> list = contentMapper.rankByFavorites(typeParam);
            default -> list = contentMapper.rankByReads(typeParam);
        }
        return list.stream().map(this::toSimpleVO).collect(Collectors.toList());
    }

    public ContentVO getHotBanner(String type) {
        String typeParam = type == null ? "" : type;
        Content content = contentMapper.getHotBannerContent(typeParam);
        if (content == null) {
            List<Content> top = contentMapper.rankByReads(typeParam);
            if (top.isEmpty()) {
                return null;
            }
            content = top.get(0);
        }
        return toSimpleVO(content);
    }

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

        List<Long> limited = new ArrayList<>(finalTagIds).subList(0, Math.min(finalTagIds.size(), 5));
        for (Long tagId : limited) {
            Tag tag = tagService.getById(tagId);
            if (tag != null && "正常".equals(tag.getStatus())) {
                ContentTag contentTag = new ContentTag();
                contentTag.setContentId(contentId);
                contentTag.setTagId(tagId);
                contentTagMapper.insert(contentTag);
            }
        }
    }

    private void throwInaccessibleException(String reason) {
        switch (reason) {
            case "deleted", "taken_down" -> throw new BusinessException(404, "内容不存在或已下架");
            case "private" -> throw new BusinessException(403, "此内容为私密内容");
            case "followers_only" -> throw new BusinessException(403, "此内容仅粉丝可见");
            default -> throw new BusinessException(403, "当前内容暂不可见");
        }
    }

    private ContentVO toVO(Content content, Long currentUserId, boolean withCommentCount) {
        ContentVO vo = new ContentVO();
        vo.setId(content.getId());
        vo.setUserId(content.getUserId());
        vo.setTitle(content.getTitle());
        vo.setContent(content.getContent());
        vo.setType(content.getType());
        vo.setStatus(content.getStatus());
        vo.setVisibility(content.getVisibility());
        vo.setViewCount(content.getViewCount());
        vo.setLikeCount(content.getLikeCount());
        vo.setCoverImage(content.getCoverImage());
        vo.setCreateTime(content.getCreateTime());

        User author = userMapper.selectById(content.getUserId());
        if (author != null) {
            vo.setUsername(author.getUsername());
            vo.setAvatar(author.getAvatar());
        }

        List<Tag> tags = tagService.getTagsByContentId(content.getId());
        vo.setTags(tags.stream().map(tag -> {
            TagVO tagVO = new TagVO();
            tagVO.setId(tag.getId());
            tagVO.setTagName(tag.getTagName());
            tagVO.setType(tag.getType());
            tagVO.setStatus(tag.getStatus());
            return tagVO;
        }).collect(Collectors.toList()));

        if (withCommentCount) {
            Long commentCount = commentMapper.selectCount(
                    new LambdaQueryWrapper<Comment>().eq(Comment::getContentId, content.getId()));
            vo.setCommentCount(commentCount.intValue());
        }

        if (currentUserId != null) {
            Long favoriteCount = favoriteMapper.selectCount(
                    new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, currentUserId)
                            .eq(Favorite::getContentId, content.getId()));
            vo.setFavorited(favoriteCount > 0);

            Long likeCount = userLikeMapper.selectCount(
                    new LambdaQueryWrapper<UserLike>()
                            .eq(UserLike::getUserId, currentUserId)
                            .eq(UserLike::getContentId, content.getId()));
            vo.setLiked(likeCount > 0);
        }

        return vo;
    }

    private ContentVO toSimpleVO(Content content) {
        ContentVO vo = new ContentVO();
        vo.setId(content.getId());
        vo.setTitle(content.getTitle());
        vo.setType(content.getType());
        vo.setViewCount(content.getViewCount());
        vo.setLikeCount(content.getLikeCount());
        vo.setCoverImage(content.getCoverImage());
        vo.setCreateTime(content.getCreateTime());

        Long favoriteCount = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getContentId, content.getId()));
        vo.setFavoriteCount(favoriteCount.intValue());

        User author = userMapper.selectById(content.getUserId());
        if (author != null) {
            vo.setUserId(author.getId());
            vo.setUsername(author.getUsername());
            vo.setAvatar(author.getAvatar());
        }
        return vo;
    }
}
