package com.video.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @TableId(type = IdType.AUTO)
    private Integer uid;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String background;
    private Integer gender;
    private String description;
    private Integer exp;
    private Double coin;
    private Integer fans;
    private Integer vip;
    private Integer state;
    private Integer role;
    private Integer auth;
    private String authMsg;
    private Date createDate;
    private Date deleteDate;
}
