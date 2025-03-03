package com.video.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.video.core.entity.Favorite;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
