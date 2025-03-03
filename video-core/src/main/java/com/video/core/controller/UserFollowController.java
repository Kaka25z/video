package com.video.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.video.core.entity.CustomResponse;
import com.video.core.service.follow.FollowService;
import com.video.core.service.follow.UserFollowService;

@RestController
@RequestMapping("/user/follow")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private FollowService followService;

    @PostMapping("/add")
    public CustomResponse followUser(@RequestParam("uid") Integer uid, @RequestParam("followUid") Integer followUid) {
        
        CustomResponse response = new CustomResponse();

        try {
            String result = userFollowService.followUser(uid, followUid);
            followService.updateUserFansCount(followUid);
            response.setData(result);
            response.setCode(200);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setCode(500);
            response.setMessage("关注失败");
            return response;
        }
        
    }

    @PostMapping("/remove")
    public CustomResponse unfollowUser(@RequestParam("uid") Integer uid, @RequestParam("followUid") Integer followUid) {
        CustomResponse response = new CustomResponse();

        try {
            String result = userFollowService.unfollowUser(uid, followUid);
            response.setData(result);
            response.setCode(200);
            return response;
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("取消关注失败");
            return response;
        }
    }

    @GetMapping("/get-all")
    public CustomResponse getAllFollow(@RequestParam("uid") Integer uid) {
        CustomResponse response = new CustomResponse();

        try {
            response.setData(followService.getFollowListWithData(uid));
            response.setCode(200);
            return response;
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("获取关注列表失败");
            return response;
        }
    }

    @GetMapping("/check-following")
    public CustomResponse checkFollowing(@RequestParam("uid") Integer uid, @RequestParam("followUid") Integer followUid) {
        CustomResponse response = new CustomResponse();

        try {
            response.setData(followService.checkFollowing(uid, followUid));
            response.setCode(200);
            return response;
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("检查关注失败");
            return response;
        }
    }
}
