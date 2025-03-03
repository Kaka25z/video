package com.video.core.service.impl.follow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.video.core.entity.Follow;
import com.video.core.entity.User;
import com.video.core.mapper.FollowMapper;
import com.video.core.mapper.UserMapper;
import com.video.core.service.follow.FollowService;
import com.video.core.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FollowServiceImpl implements FollowService{
    
    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    public void addFollowUid(Integer uid, Integer followUid) {

        Follow follow = followMapper.selectById(uid);
        if (follow != null) {
            String followUids = follow.getFollowUids();
            followUids = StringUtils.hasText(followUids) ? followUids + "," + followUid : followUid.toString();
            follow.setFollowUids(followUids);
            followMapper.updateById(follow);
        } else {
            follow = new Follow();
            follow.setUid(uid);
            follow.setFollowUids(followUid.toString());
            followMapper.insert(follow);
        }
    }

    @Override
    public void addFansUid(Integer uid, Integer fansUid) {
        Follow follow = followMapper.selectById(uid);
        if (follow != null) {
            String fansUids = follow.getFansUids();
            fansUids = StringUtils.hasText(fansUids) ? fansUids + "," + fansUid : fansUid.toString();
            follow.setFansUids(fansUids);
            followMapper.updateById(follow);
        } else {
            follow = new Follow();
            follow.setUid(uid);
            follow.setFansUids(fansUid.toString());
            followMapper.insert(follow);
        }
    }

    @Override
    public void removeFollowUid(Integer uid, Integer followUid) {
        Follow follow = followMapper.selectById(uid);
        if (follow != null) {
            String followUids = follow.getFollowUids();
            if (StringUtils.hasText(followUids)) {
                String[] followUidArray = followUids.split(",");
                StringBuilder sb = new StringBuilder();
                for (String followUidStr : followUidArray) {
                    if (!followUidStr.equals(followUid.toString())) {
                        sb.append(followUidStr).append(",");
                    }
                }
                followUids = sb.toString();
                if (followUids.endsWith(",")) {
                    followUids = followUids.substring(0, followUids.length() - 1);
                }
                follow.setFollowUids(followUids);
                followMapper.updateById(follow);
            }
        }
    }

    @Override
    public void removeFansUid(Integer uid, Integer fansUid) {

        QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);

        Follow follow = followMapper.selectOne(queryWrapper);
        if (follow != null) {
            String fansUids = follow.getFansUids();
            if (StringUtils.hasText(fansUids)) {
                List<String> fansUidList = new ArrayList<>(Arrays.asList(fansUids.split(",")));
                fansUidList.remove(fansUid.toString());
                follow.setFansUids(String.join(",", fansUidList));
                followMapper.updateById(follow);
            }
        }
    }

    @Override
    public Follow getFollowListWithData(Integer uid) {

        String key = "followList:" + uid;
        Follow follow = redisUtil.getObject(key, Follow.class);

        if (follow == null) {
            try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
                QueryWrapper<Follow> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("uid", uid);
    
                follow = followMapper.selectOne(queryWrapper);
                if (follow == null) {
                    return new Follow();
                }

                Follow finalFollow = follow;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue(key, finalFollow, 1, TimeUnit.HOURS);
                }, taskExecutor);
            }
        }

        return follow;
    }

    @Override
    public boolean checkFollowing(Integer uid, Integer followUid) {

        String key = "isFollowed:" + uid + ":" + followUid;
        Boolean isFollowed = redisUtil.getObject(key, Boolean.class);

        if (isFollowed != null) {
            return isFollowed;
        }

        Follow follow = followMapper.selectById(uid);
        if (follow != null) {
            String followUids = follow.getFollowUids();
            if (StringUtils.hasText(followUids)) {
                List<String> followUidList = Arrays.asList(followUids.split(","));
                isFollowed = followUidList.contains(followUid.toString());
            } else {
                isFollowed = false;
            }
        } else {
            isFollowed = false;
        }

        redisUtil.delValue(key);
        redisUtil.setExObjectValue(key, isFollowed, 1, TimeUnit.HOURS);

        return isFollowed;
    }

    @Override
    public int getFollowCount(Integer uid) {
        Follow follow = followMapper.selectById(uid);

        if (follow == null || follow.getFollowUids() == null) {
            return 0;
        }

        String followUids = follow.getFollowUids();
        if (!StringUtils.hasText(followUids)) {
            return 0;
        }

        String[] followUidArray = followUids.split(",");
        int followsCount = followUidArray.length;

        return followsCount;
    }

    @Override
    public int getFansCount(Integer uid) {
        Follow follow = followMapper.selectById(uid);

        if (follow == null || follow.getFansUids() == null) {
            return 0;
        }

        String fansUids = follow.getFansUids();
        if (!StringUtils.hasText(fansUids)) {
            return 0;
        }

        String[] fansUidArray = fansUids.split(",");
        int fansCount = fansUidArray.length;

        return fansCount;
    }

    @Override
    public void updateUserFansCount(Integer uid) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        User user = userMapper.selectOne(queryWrapper);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid);
        updateWrapper.setSql("fans = fans + 1");
        userMapper.update(user, updateWrapper);
    }
    
}
