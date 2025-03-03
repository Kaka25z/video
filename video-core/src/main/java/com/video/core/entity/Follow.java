package com.video.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Follow {

    @TableId
    private Integer uid;

    private String followUids;

    private String fansUids;

    private Date createdAt;
}
