package com.video.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.video.core.entity.Category;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
