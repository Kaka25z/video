package com.video.core.service.follow;

public interface UserFollowService {

    String followUser(Integer uid, Integer followUid);

    String unfollowUser(Integer uid, Integer followUid);

}
