package com.video.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.video.core.entity.CustomResponse;
import com.video.core.entity.Danmu;
import com.video.core.service.danmu.DanmuService;
import com.video.core.service.utils.CurrentUser;
import com.video.core.utils.RedisUtil;

import java.util.List;
import java.util.Set;

@RestController
public class DanmuController {
    @Autowired
    private DanmuService danmuService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CurrentUser currentUser;

    /**
     * 获取弹幕列表
     * @param vid   视频ID
     * @return  CustomResponse对象
     */
    @GetMapping("/danmu-list/{vid}")
    public CustomResponse getDanmuList(@PathVariable("vid") String vid) {
        danmuService.addDanmu(vid);
        Set<Object> idset = redisUtil.getMembers("danmu_idset:" + vid);
        List<Danmu> danmuList = danmuService.getDanmuListByIdset(idset, vid);
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(danmuList);
        return customResponse;
    }

    /**
     * 删除弹幕
     * @param id    弹幕id
     * @return  响应对象
     */
    @PostMapping("/danmu/delete")
    public CustomResponse deleteDanmu(@RequestParam("id") Integer id) {
        Integer loginUid = currentUser.getUserId();
        return danmuService.deleteDanmu(id, loginUid, currentUser.isAdmin());
    }

    @GetMapping("/danmu/pending")
    public CustomResponse getPendingDanmus() {
        List<Danmu> pendingDanmus = danmuService.getPendingDanmus();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(pendingDanmus);
        return customResponse;
    }

    @GetMapping("/danmu/approved")
    public CustomResponse getApprovedDanmus() {
        List<Danmu> approvedDanmus = danmuService.getApprovedDanmus();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(approvedDanmus);
        return customResponse;
    }

    @PostMapping("/danmu/approve/{id}")
    public void approveDanmu(@PathVariable Integer id) {
        danmuService.approveDanmu(id);
    }
}
