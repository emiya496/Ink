package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkforge.common.PageResult;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.dto.response.ContentVO;
import com.inkforge.entity.Content;
import com.inkforge.entity.Favorite;
import com.inkforge.mapper.ContentMapper;
import com.inkforge.mapper.FavoriteMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        if (content == null) throw new BusinessException(404, "内容不存在");
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getContentId, contentId));
        if (count > 0) throw new BusinessException("已收藏");
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
        if (deleted == 0) throw new BusinessException("未收藏");
    }

    public PageResult<ContentVO> listFavorites(Long userId, Integer page, Integer size) {
        IPage<Favorite> pageResult = favoriteMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreateTime));
        List<ContentVO> voList = pageResult.getRecords().stream()
                .map(f -> {
                    Content c = contentMapper.selectById(f.getContentId());
                    if (c == null) return null;
                    return contentService.getDetail(c.getId(), userId);
                })
                .filter(v -> v != null)
                .collect(Collectors.toList());
        return PageResult.of(pageResult.getTotal(), voList, page, size);
    }

    public boolean isFavorited(Long userId, Long contentId) {
        return favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getContentId, contentId)) > 0;
    }
}
