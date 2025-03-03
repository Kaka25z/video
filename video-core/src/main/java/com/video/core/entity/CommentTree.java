package com.video.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

import com.video.core.entity.dto.UserDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentTree {
    private Integer id;
    private Integer vid;
    private Integer rootId;
    private Integer parentId;
    private String content;
    private UserDTO user;
    private UserDTO toUser;
    private Integer love;
    private Integer bad;
    private List<CommentTree> replies;
    private Date createTime;
    private Long count;
}
