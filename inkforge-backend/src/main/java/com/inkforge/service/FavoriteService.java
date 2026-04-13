package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.dto.response.ContentVO;
import com.inkforge.dto.response.FavoritePageResult;
import com.inkforge.entity.Content;
import com.inkforge.entity.Favorite;
import com.inkforge.mapper.ContentMapper;
import com.inkforge.mapper.FavoriteMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ContentMapper contentMapper;
    private final ContentService contentService;

    public FavoriteService(FavoriteMapper favoriteMapper, ContentMapper contentMapper, ContentService contentService) {
        this.favoriteMapper = favoriteMapper;
        this.contentMapper = contentMapper;
        this.contentService = contentService;
    }

    public void addFavorite(Long userId, Long contentId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(404, "内容不存在");
        }
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getContentId, contentId));
        if (count > 0) {
            throw new BusinessException("已收藏");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setContentId(contentId);
        favoriteMapper.insert(favorite);
    }

    public void removeFavorite(Long userId, Long contentId) {
        int deleted = favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getContentId, contentId));
        if (deleted == 0) {
            throw new BusinessException("未收藏");
        }
    }

    public FavoritePageResult listFavorites(Long userId, Integer page, Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 10 : size;

        List<Favorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreateTime));

        List<ContentVO> visibleList = new ArrayList<>();
        Map<String, Long> hiddenByReason = new LinkedHashMap<>();

        for (Favorite favorite : favorites) {
            Content content = contentMapper.selectById(favorite.getContentId());
            String hiddenReason = contentService.getInaccessibleReason(content, userId);
            if (hiddenReason != null) {
                hiddenByReason.merge(hiddenReason, 1L, Long::sum);
                continue;
            }
            visibleList.add(contentService.getAccessibleContentSnapshot(content, userId));
        }

        int fromIndex = Math.min((safePage - 1) * safeSize, visibleList.size());
        int toIndex = Math.min(fromIndex + safeSize, visibleList.size());
        List<ContentVO> pagedList = new ArrayList<>(visibleList.subList(fromIndex, toIndex));

        long hiddenTotal = hiddenByReason.values().stream().mapToLong(Long::longValue).sum();

        return FavoritePageResult.of(
                (long) visibleList.size(),
                pagedList,
                safePage,
                safeSize,
                hiddenTotal,
                hiddenByReason);
    }

    public boolean isFavorited(Long userId, Long contentId) {
        return favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getContentId, contentId)) > 0;
    }
}
