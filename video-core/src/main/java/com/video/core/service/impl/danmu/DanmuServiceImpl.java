package com.video.core.service.impl.danmu;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.video.core.entity.CustomResponse;
import com.video.core.entity.Danmu;
import com.video.core.entity.Video;
import com.video.core.mapper.DanmuMapper;
import com.video.core.mapper.VideoMapper;
import com.video.core.service.danmu.DanmuService;
import com.video.core.service.video.VideoStatsService;
import com.video.core.utils.RedisUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class DanmuServiceImpl implements DanmuService {

    @Autowired
    private DanmuMapper danmuMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private RedisUtil redisUtil;

    private static final String DANMU_LIST_KEY_PREFIX = "danmu_list:";

    /**
     * 根据弹幕ID集合查询弹幕列表
     * @param idset 弹幕ID集合
     * @return  弹幕列表
     */
    @Override
    public List<Danmu> getDanmuListByIdset(Set<Object> idset, String vid) {

        String key = DANMU_LIST_KEY_PREFIX + vid;

        List<Danmu> danmuList = redisUtil.getObject(key, List.class);

        if (danmuList != null) {
            return danmuList;
        }

        if (idset == null || idset.size() == 0) {
            return null;
        }
        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", idset).eq("state", 1).orderBy(true, true, "RAND()").last("LIMIT 1500");
        danmuList = danmuMapper.selectList(queryWrapper);
        
        if (danmuList.size() > 1000) {
            redisUtil.setExObjectValue(key, danmuList, 1, TimeUnit.HOURS);
            return danmuList;
        } else {
            return danmuList;
        }
    }

    @Override
    @Transactional
    public CustomResponse deleteDanmu(Integer id, Integer uid, boolean isAdmin) {
        CustomResponse customResponse = new CustomResponse();
        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).ne("state", 3);
        Danmu danmu = danmuMapper.selectOne(queryWrapper);
        if (danmu == null) {
            customResponse.setCode(404);
            customResponse.setMessage("弹幕不存在");
            return customResponse;
        }
        // 判断该用户是否有权限删除这条评论
        Video video = videoMapper.selectById(danmu.getVid());
        if (Objects.equals(danmu.getUid(), uid) || isAdmin || Objects.equals(video.getUid(), uid)) {
            // 删除弹幕
            UpdateWrapper<Danmu> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id).set("state", 3);
            danmuMapper.update(null, updateWrapper);
            videoStatsService.updateStats(danmu.getVid(), "danmu", false, 1);
            redisUtil.delMember("danmu_idset:" + danmu.getVid(), id);
        } else {
            customResponse.setCode(403);
            customResponse.setMessage("你无权删除该条评论");
        }
        return customResponse;
    }

    @Override
    public void addDanmu(String vid) {
        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid).eq("state", 1).select("id");
        List<Danmu> danmuIdList = danmuMapper.selectList(queryWrapper);
        for (Danmu danmu : danmuIdList) {
            redisUtil.addMember("danmu_idset:" + vid, danmu.getId());
        }
    }

    @Override
    public List<Danmu> getPendingDanmus() {
        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("state", 2); // 2表示待审核
        return danmuMapper.selectList(queryWrapper);
    }

    @Override
    public List<Danmu> getApprovedDanmus() {
        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("state", 1); // 1表示通过审核
        return danmuMapper.selectList(queryWrapper);
    }

    @Override
    public void approveDanmu(Integer id) {
        UpdateWrapper<Danmu> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).set("state", 1); // 1表示审核通过
        danmuMapper.update(null, updateWrapper);
    }
}
