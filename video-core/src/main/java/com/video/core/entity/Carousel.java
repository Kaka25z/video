package com.video.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Carousel {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String url;

    private String title;

    private String color;

    private String target;
}
