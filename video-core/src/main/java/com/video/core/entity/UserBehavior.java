package com.video.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBehavior {

    private Integer uid;

    private Integer videoId;
    
    private Integer behaviorType; // 1:观看, 2:点赞, 3:评论
}
