package com.inkforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inkforge.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
