package com.video.core.service.video;

import java.util.List;

import com.video.core.entity.UserVideo;
import com.video.core.entity.Video;

public interface UserVideoService {
    /**
     * 更新播放次数以及最近播放时间，顺便返回记录信息，没有记录则创建新记录
     * @param uid   用户ID
     * @param vid   视频ID
     * @return 更新后的数据信息
     */
    UserVideo updatePlay(Integer uid, Integer vid);

    /**
     * 点赞或点踩，返回更新后的信息
     * @param uid   用户ID
     * @param vid   视频ID
     * @param isLove    赞还是踩 true赞 false踩
     * @param isSet     设置还是取消  true设置 false取消
     * @return  更新后的信息
     */
    UserVideo setLoveOrUnlove(Integer uid, Integer vid, boolean isLove, boolean isSet);

    /**
     * 收藏或取消收藏
     * @param uid   用户ID
     * @param vid   视频ID
     * @param isCollect 是否收藏 true收藏 false取消
     */
    void collectOrCancel(Integer uid, Integer vid, boolean isCollect);

    UserVideo getUserVideoByVid(Integer vid, Integer uid);

    UserVideo coin(Integer uid, Integer vid);

    List<Integer> getRecentCoinedVideos(Integer uid, Integer offset, Integer quantity);
}
