package com.video.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.video.core.entity.Chat;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMapper extends BaseMapper<Chat> {
}
