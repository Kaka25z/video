package com.video.core.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.video.core.entity.DailyUserPlayCount;
import com.video.core.entity.IMResponse;
import com.video.core.entity.User;
import com.video.core.entity.UserVideo;
import com.video.core.entity.Video;
import com.video.core.im.IMServer;
import com.video.core.mapper.DailyUserPlayCountMapper;
import com.video.core.mapper.UserMapper;
import com.video.core.mapper.UserVideoMapper;
import com.video.core.mapper.VideoMapper;
import com.video.core.service.message.MsgUnreadService;
import com.video.core.service.utils.CurrentUser;
import com.video.core.service.video.UserVideoService;
import com.video.core.service.video.VideoStatsService;
import com.video.core.utils.RedisUtil;

import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class UserVideoServiceImpl implements UserVideoService {

    @Autowired
    private UserVideoMapper userVideoMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private MsgUnreadService msgUnreadService;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private DailyUserPlayCountMapper dailyUserPlayCountMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 更新播放次数以及最近播放时间，顺便返回记录信息，没有记录则创建新记录
     * @param uid   用户ID
     * @param vid   视频ID
     * @return 更新后的数据信息
     */
    @Override
    public UserVideo updatePlay(Integer uid, Integer vid) {
        UserVideo userVideo = null;
        if (uid != null) {
            QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", uid).eq("vid", vid);
            userVideo = userVideoMapper.selectOne(queryWrapper);
            if (userVideo == null) {
                // 记录不存在，创建新记录
                userVideo = new UserVideo(null, uid, vid, 1, 0, 0, 0, 0, new Date(), null, null);
                userVideoMapper.insert(userVideo);
            } else if (System.currentTimeMillis() - userVideo.getPlayTime().getTime() <= 30000) {
                // 如果最近30秒内播放过则不更新记录，直接返回
                return userVideo;
            } else {
                userVideo.setPlay(userVideo.getPlay() + 1);
                userVideo.setPlayTime(new Date());
                userVideoMapper.updateById(userVideo);
            }

            LocalDate today = LocalDate.now();
            DailyUserPlayCount dailyUserPlayCount = dailyUserPlayCountMapper.selectOne(
                    new QueryWrapper<DailyUserPlayCount>().eq("uid", uid).eq("date", today)
            );
            if (dailyUserPlayCount == null) {
                dailyUserPlayCount = new DailyUserPlayCount(null, uid, 1, today);
                dailyUserPlayCountMapper.insert(dailyUserPlayCount);
            } else {
                dailyUserPlayCount.setPlayCount(dailyUserPlayCount.getPlayCount() + 1);
                dailyUserPlayCountMapper.updateById(dailyUserPlayCount);
            }

            // 异步线程更新video表和redis
            CompletableFuture.runAsync(() -> {
                redisUtil.zset("user_video_history:" + uid, vid);   // 添加到/更新观看历史记录
                videoStatsService.updateStats(vid, "play", true, 1);
            }, taskExecutor);
        } else {

            CompletableFuture.runAsync(() -> {
                videoStatsService.updateStats(vid, "play", true, 1);
            }, taskExecutor);
        }

        return userVideo;
    }

    /**
     * 点赞或点踩，返回更新后的信息
     * @param uid   用户ID
     * @param vid   视频ID
     * @param isLove    赞还是踩 true赞 false踩
     * @param isSet     设置还是取消  true设置 false取消
     * @return  更新后的信息
     */
    @Override
    public UserVideo setLoveOrUnlove(Integer uid, Integer vid, boolean isLove, boolean isSet) {
        String key = "love_video:" + uid;
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);
        if (isLove && isSet) {
            // 点赞
            if (userVideo.getLove() == 1) {
                // 原本就点了赞就直接返回
                return userVideo;
            }
            // 插入点赞记录
            userVideo.setLove(1);
            UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).eq("vid", vid);
            updateWrapper.setSql("love = 1");
            updateWrapper.set("love_time", new Date());
            if (userVideo.getUnlove() == 1) {
                // 原本点了踩，要取消踩
                userVideo.setUnlove(0);
                updateWrapper.setSql("unlove = 0");
                CompletableFuture.runAsync(() -> {
                    videoStatsService.updateGoodAndBad(vid, true);
                }, taskExecutor);
            } else {
                // 原本没点踩，只需要点赞就行
                CompletableFuture.runAsync(() -> {
                    videoStatsService.updateStats(vid, "good", true, 1);
                }, taskExecutor);
            }
            redisUtil.zset(key, vid);   // 添加点赞记录
            userVideoMapper.update(null, updateWrapper);
            // 通知up主视频被赞了
            CompletableFuture.runAsync(() -> {
                // 查询UP主uid
                Video video = videoMapper.selectById(vid);
                if(!Objects.equals(video.getUid(), uid)) {
                    // 更新最新被点赞的视频
                    redisUtil.zset("be_loved_zset:" + video.getUid(), vid);
                    msgUnreadService.addOneUnread(video.getUid(), "love");
                    // netty 通知未读消息
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", "接收");
                    Set<Channel> channels = IMServer.userChannel.get(video.getUid());
                    if (channels != null) {
                        for (Channel channel: channels) {
                            channel.writeAndFlush(IMResponse.message("love", map));
                        }
                    }
                }
            }, taskExecutor);
        } else if (isLove) {
            // 取消点赞
            if (userVideo.getLove() == 0) {
                // 原本就没有点赞就直接返回
                return userVideo;
            }
            // 取消赞
            userVideo.setLove(0);
            UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).eq("vid", vid);
            updateWrapper.setSql("love = 0");
            userVideoMapper.update(null, updateWrapper);
            redisUtil.zsetDelMember(key, vid);  // 移除点赞记录
            CompletableFuture.runAsync(() -> {
                videoStatsService.updateStats(vid, "good", false, 1);
            }, taskExecutor);
        } else if (isSet) {
            // 点踩
            if (userVideo.getUnlove() == 1) {
                // 原本就点了踩就直接返回
                return userVideo;
            }
            // 更新用户点踩记录
            userVideo.setUnlove(1);
            UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).eq("vid", vid);
            updateWrapper.setSql("unlove = 1");
            if (userVideo.getLove() == 1) {
                // 原本点了赞，要取消赞
                userVideo.setLove(0);
                updateWrapper.setSql("love = 0");
                redisUtil.zsetDelMember(key, vid);  // 移除点赞记录
                CompletableFuture.runAsync(() -> {
                    videoStatsService.updateGoodAndBad(vid, false);
                }, taskExecutor);
            } else {
                // 原本没点赞，只需要点踩就行
                CompletableFuture.runAsync(() -> {
                    videoStatsService.updateStats(vid, "bad", true, 1);
                }, taskExecutor);
            }
            userVideoMapper.update(null, updateWrapper);
        } else {
            // 取消点踩
            if (userVideo.getUnlove() == 0) {
                // 原本就没有点踩就直接返回
                return userVideo;
            }
            // 取消用户点踩记录
            userVideo.setUnlove(0);
            UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).eq("vid", vid);
            updateWrapper.setSql("unlove = 0");
            userVideoMapper.update(null, updateWrapper);
            CompletableFuture.runAsync(() -> {
                videoStatsService.updateStats(vid, "bad", false, 1);
            }, taskExecutor);
        }
        return userVideo;
    }

    /**
     * 收藏或取消收藏
     * @param uid   用户ID
     * @param vid   视频ID
     * @param isCollect 是否收藏 true收藏 false取消
     * @return  返回更新后的信息
     */
    @Override
    public void collectOrCancel(Integer uid, Integer vid, boolean isCollect) {
        UpdateWrapper<UserVideo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uid", uid).eq("vid", vid);
        if (isCollect) {
            updateWrapper.setSql("collect = 1");
        } else {
            updateWrapper.setSql("collect = 0");
        }
        CompletableFuture.runAsync(() -> {
            videoStatsService.updateStats(vid, "collect", isCollect, 1);
        }, taskExecutor);
        userVideoMapper.update(null, updateWrapper);
    }

    @Override
    public UserVideo getUserVideoByVid(Integer vid, Integer uid) {

        String key = "user_video:" + vid + ":" + uid;

        // UserVideo userVideo = redisUtil.getObject(key, UserVideo.class);
        // if (!userVideo.equals(null)) {
        //     return userVideo;
        // }

        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("vid", vid).eq("uid", uid);
        final UserVideo finaluserVideo = userVideoMapper.selectOne(queryWrapper);

        if (finaluserVideo != null) {
            CompletableFuture.runAsync(() -> {
                redisUtil.setExObjectValue(key, finaluserVideo);
            }, taskExecutor);
        }
        return finaluserVideo;

    }

    /**
     * 投币操作，用户只能投一个币
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 更新后的信息
     */
    @Override
    public UserVideo coin(Integer uid, Integer vid) {
        int coinNum = 1;

        User user = userMapper.selectById(uid);
        if (user == null || user.getCoin() < coinNum) {
            return null;
        }

        UserVideo userVideo = userVideoMapper.selectOne(new QueryWrapper<UserVideo>().eq("uid", uid).eq("vid", vid));
        if (userVideo == null) {
            userVideo = new UserVideo();
            userVideo.setUid(uid);
            userVideo.setVid(vid);
            userVideo.setCoin(coinNum);
            userVideo.setCoinTime(new Date());
            userVideoMapper.insert(userVideo);
        } else {
            if (userVideo.getCoin() == 1) {
                return userVideo;
            }
            userVideo.setCoin(coinNum);
            userVideo.setCoinTime(new Date());
            userVideoMapper.updateById(userVideo);
        }

        user.setCoin(user.getCoin() - coinNum);
        userMapper.updateById(user);

        CompletableFuture.runAsync(() -> {
            videoStatsService.updateStats(vid, "coin", true, coinNum);
        }, taskExecutor);

        return userVideo;

    }

    /**
     * 获取用户最近投币的视频
     * @param uid 用户ID
     * @param offset 偏移量
     * @param quantity 数量
     * @return 最近投币的视频列表
     */
    @Override
    public List<Integer> getRecentCoinedVideos(Integer uid, Integer offset, Integer quantity) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).isNotNull("coin_time").orderByDesc("coin_time").last("LIMIT " + offset + "," + quantity);
        List<UserVideo> userVideos = userVideoMapper.selectList(queryWrapper);

        List<Integer> videoIds = userVideos.stream().map(UserVideo::getVid).collect(Collectors.toList());
        if (videoIds.isEmpty()) {
            return Collections.emptyList();
        }

        return videoIds;
    }
}
