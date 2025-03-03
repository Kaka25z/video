package com.video.core.service.impl.video;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.video.core.entity.UserVideo;
import com.video.core.entity.Video;
import com.video.core.mapper.UserVideoMapper;
import com.video.core.mapper.VideoMapper;
import com.video.core.service.user.UserSimilarityService;
import com.video.core.service.video.RecommendationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {


    @Autowired
    private UserVideoMapper userVideoMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserSimilarityService userSimilarityService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SIMILARITY_KEY_PREFIX = "similarity:";

    @Override
    public List<Integer> recommendVideosUserCF(Integer uid) {
        
        Map<Integer, Map<Integer, Double>> userSimilarityMatrix = userSimilarityService.calculateSimilarity();

        Map<Integer, Double> similarityMap = userSimilarityMatrix.getOrDefault(uid, new HashMap<>());

        List<Map.Entry<Integer, Double>> sortedSimilarUsers = similarityMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        Set<Integer> recommendedVideos = new HashSet<>();
        Random random = new Random();
        for (Map.Entry<Integer, Double> entry : sortedSimilarUsers) {
            Integer similarUserId = entry.getKey();
            List<UserVideo> userVideos = getUserVideos(similarUserId);
            for (UserVideo userVideo : userVideos) {
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("vid", userVideo.getVid()).eq("status", 1);
                Video video = videoMapper.selectOne(queryWrapper);
                if (video != null && random.nextDouble() > 0.2) {
                    recommendedVideos.add(userVideo.getVid());
                }
            }

            if (recommendedVideos.size() >= 10) {
                break;
            }
        }

        return new ArrayList<>(recommendedVideos);
    }

    @Override
    public List<Integer> recommendVideosBasedOnContent(Integer uid) {
        List<UserVideo> userVideos = getUserVideos(uid);

        if (userVideos == null || userVideos.isEmpty()) {
            return new ArrayList<>();
        }

        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("vid", "mc_id", "sc_id", "tags").eq("status", 1);
        List<Video> allVideos = videoMapper.selectList(queryWrapper);

        if (allVideos == null || allVideos.isEmpty()) {
            return new ArrayList<>();
        }
    

        QueryWrapper<Video> queryWrapper2 = new QueryWrapper<>();
        List<Video> watchedVideos = new ArrayList<>();

        for (UserVideo userVideo : userVideos) {
            queryWrapper2.eq("vid", userVideo.getVid()).eq("status", 1);
            Video video = videoMapper.selectOne(queryWrapper2);
            if (video != null) {
                watchedVideos.add(video);
            }
        }

        Random random = new Random();
        List<Video> recommendedVideos = allVideos.stream()
                .filter(video -> isSimilar(watchedVideos, video) && random.nextDouble() > 0.2)
                .collect(Collectors.toList());

        return recommendedVideos.stream().map(Video::getVid).collect(Collectors.toList());

    }

    @Override
    public List<Integer> hybridRecommendVideos(Integer uid) {

        List<Integer> userCFRecommendations = recommendVideosUserCF(uid);
        List<Integer> contentBasedRecommendations = recommendVideosBasedOnContent(uid);

        double w1 = 0.6;
        double w2 = 0.4;
        Random random = new Random();

        Map<Integer, Double> weightedScores = new HashMap<>();
        for (int i = 0; i < userCFRecommendations.size(); i++) {
            double score = w1 * (userCFRecommendations.size() - i) + random.nextDouble() * 0.05;
            weightedScores.put(userCFRecommendations.get(i), weightedScores.getOrDefault(userCFRecommendations.get(i), 0.0) + score);
        }
    
        for (int i = 0; i < contentBasedRecommendations.size(); i++) {
            double score = w2 * (contentBasedRecommendations.size() - i) + random.nextDouble() * 0.05;
            weightedScores.put(contentBasedRecommendations.get(i), weightedScores.getOrDefault(contentBasedRecommendations.get(i), 0.0) + score);
        }
    
        List<Map.Entry<Integer, Double>> sortedRecommendations = weightedScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
    
        List<Integer> finalRecommendations = sortedRecommendations.stream().map(Map.Entry::getKey).collect(Collectors.toList());

        return finalRecommendations;
    }


    private List<UserVideo> getUserVideos(Integer uid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        return userVideoMapper.selectList(queryWrapper);
    }

    private boolean isSimilar(List<Video> watchedVideos, Video video) {

        String redisKey = SIMILARITY_KEY_PREFIX + generateKey(watchedVideos, video);

        Boolean cachedSimilarity = (Boolean) redisTemplate.opsForValue().get(redisKey);
        if (cachedSimilarity != null) {
            return cachedSimilarity;
        }

        Set<String> watchedTags = watchedVideos.stream()
                .flatMap(v -> Arrays.stream(v.getTags().split("\n")))
                .collect(Collectors.toSet());

        Set<String> watchedMcIds = watchedVideos.stream()
                .map(Video::getMcId)
                .collect(Collectors.toSet());

        Set<String> watchedScIds = watchedVideos.stream()
                .map(Video::getScId)
                .collect(Collectors.toSet());

        boolean hasCommonTags = Arrays.stream(video.getTags().split("\n"))
                .anyMatch(watchedTags::contains);
        boolean isSameMcId = watchedMcIds.contains(video.getMcId());
        boolean isSameScId = watchedScIds.contains(video.getScId());

        boolean isSimilar = hasCommonTags || isSameMcId || isSameScId;

        redisTemplate.opsForValue().set(redisKey, isSimilar, 10, TimeUnit.MINUTES);

        return isSimilar;
    }

    private String generateKey(List<Video> watchedVideos, Video video) {
        String watchedVideosKey = watchedVideos.stream()
                .map(Video::getVid)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return watchedVideosKey + ":" + video.getVid();
    }
}
