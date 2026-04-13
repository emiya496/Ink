package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkforge.common.PageResult;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.dto.response.UserProfileVO;
import com.inkforge.entity.User;
import com.inkforge.entity.UserFollow;
import com.inkforge.mapper.UserFollowMapper;
import com.inkforge.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;

    public FollowService(UserFollowMapper userFollowMapper, UserMapper userMapper) {
        this.userFollowMapper = userFollowMapper;
        this.userMapper = userMapper;
    }

    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) throw new BusinessException("不能关注自己");
        // 检查被关注用户是否存在
        if (userMapper.selectById(followeeId) == null) throw new BusinessException(404, "用户不存在");
        // 已关注则忽略
        long existing = userFollowMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
                        .eq(UserFollow::getFolloweeId, followeeId));
        if (existing > 0) return;
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        userFollowMapper.insert(follow);
    }

    /** 移除粉丝：当前用户（followeeId）将 followerId 从自己的粉丝列表中移除 */
    public void removeFollower(Long currentUserId, Long followerId) {
        userFollowMapper.delete(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
                        .eq(UserFollow::getFolloweeId, currentUserId));
    }

    public void unfollow(Long followerId, Long followeeId) {
        userFollowMapper.delete(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
                        .eq(UserFollow::getFolloweeId, followeeId));
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        if (followerId == null) return false;
        return userFollowMapper.selectCount(
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, followerId)
                        .eq(UserFollow::getFolloweeId, followeeId)) > 0;
    }

    public PageResult<UserProfileVO> getFollowers(Long userId, int page, int size) {
        var followPage = userFollowMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFolloweeId, userId)
                        .orderByDesc(UserFollow::getCreateTime));
        List<UserProfileVO> list = followPage.getRecords().stream()
                .map(f -> toSimpleVO(f.getFollowerId()))
                .filter(v -> v != null)
                .collect(Collectors.toList());
        return PageResult.of(followPage.getTotal(), list, page, size);
    }

    public PageResult<UserProfileVO> getFollowing(Long userId, int page, int size) {
        var followPage = userFollowMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, userId)
                        .orderByDesc(UserFollow::getCreateTime));
        List<UserProfileVO> list = followPage.getRecords().stream()
                .map(f -> toSimpleVO(f.getFolloweeId()))
                .filter(v -> v != null)
                .collect(Collectors.toList());
        return PageResult.of(followPage.getTotal(), list, page, size);
    }

    private UserProfileVO toSimpleVO(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return null;
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setFollowersCount(userFollowMapper.countFollowers(userId));
        vo.setFollowingCount(userFollowMapper.countFollowing(userId));
        return vo;
    }
}
