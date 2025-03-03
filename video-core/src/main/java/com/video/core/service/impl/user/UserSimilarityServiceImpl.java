package com.video.core.service.impl.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.video.core.entity.UserVideo;
import com.video.core.mapper.UserVideoMapper;
import com.video.core.service.user.UserSimilarityService;
import com.video.core.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserSimilarityServiceImpl implements UserSimilarityService {

    @Autowired
    private UserVideoMapper userVideoMapper;

    @Override
    public Map<Integer, Map<Integer, Double>> calculateSimilarity() {

        Map<Integer, Set<Integer>> itemUserMap = buildItemUserMap();
        Map<Integer, Map<Integer, Integer>> userItemIntersectionMatrix = buildUserItemIntersectionMatrix(itemUserMap);
        Map<Integer, Map<Integer, Double>> userSimilarityMatrix = calculateAllUserSimilarities(userItemIntersectionMatrix);

        return userSimilarityMatrix;
    }

    private List<UserVideo> getUserVideos(Integer uid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        return userVideoMapper.selectList(queryWrapper);
    }

    private Map<Integer, Set<Integer>> buildItemUserMap() {
        List<UserVideo> userVideos = userVideoMapper.selectList(null);
        Map<Integer, Set<Integer>> itemUserMap = new HashMap<>();

        for (UserVideo userVideo : userVideos) {
            itemUserMap.computeIfAbsent(userVideo.getVid(), k -> new HashSet<>()).add(userVideo.getUid());
        }

        return itemUserMap;
    }

    private Map<Integer, Map<Integer, Integer>> buildUserItemIntersectionMatrix(Map<Integer, Set<Integer>> itemUserMap) {
        Map<Integer, Map<Integer, Integer>> userItemIntersectionMatrix = new HashMap<>();

        for (Set<Integer> users : itemUserMap.values()) {
            for (Integer user1 : users) {
                for (Integer user2 : users) {
                    if (!user1.equals(user2)) {
                        userItemIntersectionMatrix
                            .computeIfAbsent(user1, k -> new HashMap<>())
                            .merge(user2, 1, Integer::sum);
                    }
                }
            }
        }

        return userItemIntersectionMatrix;
    }

    private int getUserNorm(Integer uid) {
        List<UserVideo> userVideos = getUserVideos(uid);
        int norm = 0;

        for (UserVideo userVideo : userVideos) {
            norm += userVideo.getLove() * userVideo.getLove() +
                    userVideo.getUnlove() * userVideo.getUnlove() +
                    userVideo.getCollect() * userVideo.getCollect() +
                    userVideo.getPlay() * userVideo.getPlay();
        }

        return norm;
    }

    private Map<Integer, Map<Integer, Double>> calculateAllUserSimilarities(Map<Integer, Map<Integer, Integer>> userItemIntersectionMatrix) {
        Map<Integer, Map<Integer, Double>> userSimilarityMatrix = new HashMap<>();

        for (Integer user1 : userItemIntersectionMatrix.keySet()) {
            for (Integer user2 : userItemIntersectionMatrix.get(user1).keySet()) {
                double similarity = calculateUserSimilarity(user1, user2, userItemIntersectionMatrix);
                userSimilarityMatrix
                    .computeIfAbsent(user1, k -> new HashMap<>())
                    .put(user2, similarity);
            }
        }

        return userSimilarityMatrix;
    }

    private double calculateUserSimilarity(Integer uid1, Integer uid2, Map<Integer, Map<Integer, Integer>> userItemIntersectionMatrix) {
        int intersection = userItemIntersectionMatrix.getOrDefault(uid1, new HashMap<>()).getOrDefault(uid2, 0);

        int normA = getUserNorm(uid1);
        int normB = getUserNorm(uid2);

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return intersection / (Math.sqrt(normA) * Math.sqrt(normB));
    }

}
