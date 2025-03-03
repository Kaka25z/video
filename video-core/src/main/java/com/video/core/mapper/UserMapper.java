package com.video.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.video.core.entity.User;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
