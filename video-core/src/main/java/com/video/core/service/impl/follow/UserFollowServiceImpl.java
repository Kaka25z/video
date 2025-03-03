package com.video.core.service.impl.follow;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.video.core.entity.Follow;
import com.video.core.mapper.FollowMapper;
import com.video.core.service.follow.FollowService;
import com.video.core.service.follow.UserFollowService;
import com.video.core.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserFollowServiceImpl implements UserFollowService{

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private FollowService followService;

    @Override
    public String followUser(Integer uid, Integer followUid) {
        if (uid.equals(followUid)) {
            return "不能关注自己";
        }

        followService.addFollowUid(uid, followUid);
        followService.addFansUid(followUid, uid);

        redisUtil.delValue("isFollowed:" + uid + ":" + followUid);

        return "关注成功";

    }

    @Override
    public String unfollowUser(Integer uid, Integer followUid) {

        QueryWrapper<Follow> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("uid", uid);
        Follow follow = followMapper.selectOne(followQueryWrapper);

    
        if (follow == null || !Arrays.asList(follow.getFollowUids().split(",")).contains(String.valueOf(uid))) {
            return "未关注该用户";
        }

        followService.removeFollowUid(uid, followUid);
        followService.removeFansUid(followUid, uid);

        return "取消关注成功";
    }
}
