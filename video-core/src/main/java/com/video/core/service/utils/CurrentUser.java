package com.video.core.service.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.video.core.entity.User;
import com.video.core.mapper.UserMapper;
import com.video.core.service.impl.user.UserDetailsImpl;
import com.video.core.utils.RedisUtil;

@Service
public class CurrentUser {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前登录用户的uid，也是JWT认证的一环
     * @return 当前登录用户的uid
     */
    public Integer getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;
            UserDetailsImpl loginUser = (UserDetailsImpl) authenticationToken.getPrincipal();
            User suser = loginUser.getUser();   // 这里的user是登录时存的security:user，因为是静态数据，可能会跟实际的有区别，所以只能用作获取uid用
            return suser.getUid();
        } else {
            throw new IllegalStateException("当前用户未登录");
        }
    }

    /**
     * 判断当前用户是否管理员
     * @return  是否管理员 true/false
     */
    public Boolean isAdmin() {
        Integer uid = getUserId();
        User user = redisUtil.getObject("user:" + uid, User.class);
        if (user == null) {
            user = userMapper.selectById(uid);
            redisUtil.setExObjectValue("user:" + user.getUid(), user);
        }
        return (user.getRole() == 1 || user.getRole() == 2);
    }
}
