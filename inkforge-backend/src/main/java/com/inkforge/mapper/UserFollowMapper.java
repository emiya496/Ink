package com.inkforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inkforge.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    @Select("SELECT COUNT(*) FROM user_follow WHERE followee_id = #{userId}")
    long countFollowers(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    long countFollowing(@Param("userId") Long userId);
}
