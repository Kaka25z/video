package com.video.core.service.follow;

import com.video.core.entity.Follow;

public interface FollowService {

    void addFollowUid(Integer uid, Integer followUid);

    void addFansUid(Integer uid, Integer fansUid);

    void removeFollowUid(Integer uid, Integer followUid);

    void removeFansUid(Integer uid, Integer fansUid);

    Follow getFollowListWithData(Integer uid);

    boolean checkFollowing(Integer uid, Integer followUid);

    int getFollowCount(Integer uid);

    int getFansCount(Integer uid);

    void updateUserFansCount(Integer uid);
}
