package com.video.core.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.video.core.entity.CustomResponse;
import com.video.core.entity.DailyUserPlayCount;
import com.video.core.entity.FavoriteVideo;
import com.video.core.entity.UserVideo;
import com.video.core.entity.Video;
import com.video.core.mapper.DailyUserPlayCountMapper;
import com.video.core.mapper.FavoriteVideoMapper;
import com.video.core.service.utils.CurrentUser;
import com.video.core.service.video.RecommendationService;
import com.video.core.service.video.UserVideoService;
import com.video.core.service.video.VideoService;
import com.video.core.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class VideoController {
    @Autowired
    private VideoService videoService;

    @Autowired
    private UserVideoService userVideoService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private FavoriteVideoMapper favoriteVideoMapper;

    @Autowired
    private DailyUserPlayCountMapper dailyUserPlayCountMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostMapping("/video/change/status")
    public CustomResponse updateStatus(@RequestParam("vid") Integer vid,
                                       @RequestParam("status") Integer status) {
        try {
            return videoService.updateVideoStatus(vid, status);
        } catch (Exception e) {
            e.printStackTrace();
            return new CustomResponse(500, "操作失败", null);
        }
    }

    @GetMapping("/video/random/visitor")
    public CustomResponse randomVideosForVisitor() {
        CustomResponse customResponse = new CustomResponse();
        int count = 11;
        Set<Object> idSet = redisUtil.srandmember("video_status:1", count);
        List<Map<String, Object>> videoList = new ArrayList<>();
        if (idSet != null && !idSet.isEmpty()) {
            videoList = videoService.getVideosWithDataByIds(idSet, 1, count);
            // 随机打乱列表顺序
            Collections.shuffle(videoList);
        }
        customResponse.setData(videoList);
        return customResponse;
    }

    @GetMapping("/video/recommend")
    public CustomResponse recommendVideos() {
        CustomResponse customResponse = new CustomResponse();
        Integer uid = currentUser.getUserId();
        List<Integer> videoIds = recommendationService.hybridRecommendVideos(uid);
        Set<Object> uniqueVideoIds = new HashSet<>(videoIds);

        int totalVideos = 11;
        int randomCount = totalVideos - uniqueVideoIds.size();
        if (randomCount < 3) {
            randomCount = 3;
        }

        Set<Object> randomIdSet = redisUtil.srandmember("video_status:1", randomCount);

        if (randomIdSet != null && !randomIdSet.isEmpty()) {
            uniqueVideoIds.addAll(randomIdSet.stream().map(id -> (Integer) id).collect(Collectors.toList()));
        }

        while (uniqueVideoIds.size() < totalVideos) {
            randomIdSet = redisUtil.srandmember("video_status:1", totalVideos - uniqueVideoIds.size());
            if (randomIdSet != null && !randomIdSet.isEmpty()) {
                uniqueVideoIds.addAll(randomIdSet.stream().map(id -> (Integer) id).collect(Collectors.toList()));
            } else {
                break;
            }
        }


        List<Map<String, Object>> videoList = new ArrayList<>();
        if (!uniqueVideoIds.isEmpty()) {
            videoList = videoService.getVideosWithDataByIds(uniqueVideoIds, 1, totalVideos);
            Collections.shuffle(videoList);
        }

        customResponse.setData(videoList);
    
        return customResponse;
    }

    @GetMapping("/video/cumulative/recommend")
    public CustomResponse cumulativeRecommendVideos(@RequestParam("vids") String vids) {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = new HashMap<>();
        List<Integer> vidsList = new ArrayList<>();

        Set<Object> set = redisUtil.getMembers("video_status:1");
    
        if (vids.trim().length() > 0) {
            vidsList = Arrays.stream(vids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
    
        Integer uid = currentUser.getUserId();
        
        List<Integer> recommendVideoIds = recommendationService.hybridRecommendVideos(uid);
        
        // log.info("recommendVideoIds: {}", recommendVideoIds);

        List<Integer> recommendedVideoIds = recommendationService.hybridRecommendVideos(uid);

        // log.info("recommendedVideoIds: {}", recommendedVideoIds);

        recommendedVideoIds.removeAll(vidsList);
        recommendedVideoIds.removeAll(recommendVideoIds);

        // log.info("recommendedVideoIds after remove: {}", recommendedVideoIds);
        Set<Object> subList = new HashSet<>();
        Set<Object> idSet = new HashSet<>();
        Random random = new Random();
        for (int i = 0; i < 5 && !set.isEmpty(); i++) {
            Object[] arr = set.toArray();
            int randomIndex = random.nextInt(set.size());
            idSet.add(arr[randomIndex]);
            set.remove(arr[randomIndex]);
        }

        if (!idSet.isEmpty()) {
            subList = idSet;
        }
        
        List<Map<String, Object>> videoList = new ArrayList<>();
        if (!recommendedVideoIds.isEmpty()) {
            subList = recommendedVideoIds.stream().limit(10).collect(Collectors.toSet());
            videoList = videoService.getVideosWithDataByIds(subList, 1, 10);
            Collections.shuffle(videoList);
        }

    
        map.put("videos", videoList);
        map.put("vids", recommendedVideoIds.stream().limit(10).collect(Collectors.toList()));
        map.put("more", recommendedVideoIds.size() > 10);
        customResponse.setData(map);
        return customResponse;
    }

    @GetMapping("/video/cumulative/visitor")
    public CustomResponse cumulativeVideosForVisitor(@RequestParam("vids") String vids) {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = new HashMap<>();
        List<Integer> vidsList = new ArrayList<>();
        if (vids.trim().length() > 0) {
            vidsList = Arrays.stream(vids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
        Set<Object> set = redisUtil.getMembers("video_status:1");
        if (set == null) {
            map.put("videos", new ArrayList<>());
            map.put("vids", new ArrayList<>());
            map.put("more", false);
            customResponse.setData(map);
            return customResponse;
        }
        vidsList.forEach(set::remove);
        Set<Object> idSet = new HashSet<>();
        Random random = new Random();
        // 随机获取10个vid
        for (int i = 0; i < 10 && !set.isEmpty(); i++) {
            Object[] arr = set.toArray();
            int randomIndex = random.nextInt(set.size());
            idSet.add(arr[randomIndex]);
            set.remove(arr[randomIndex]);
        }
        List<Map<String, Object>> videoList = new ArrayList<>();
        if (!idSet.isEmpty()) {
            videoList = videoService.getVideosWithDataByIds(idSet, 1, 10);
            Collections.shuffle(videoList);
        }
        map.put("videos", videoList);
        map.put("vids", idSet);
        if (!set.isEmpty()) {
            map.put("more", true);
        } else {
            map.put("more", false);
        }
        customResponse.setData(map);
        return customResponse;
    }

    @GetMapping("/video/getone")
    public CustomResponse getOneVideo(@RequestParam("vid") Integer vid) {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = videoService.getVideoWithDataById(vid);
        if (map == null) {
            customResponse.setCode(404);
            customResponse.setMessage("视频不存在");
            return customResponse;
        }
        Video video = (Video) map.get("video");
        if (video.getStatus() != 1) {
            customResponse.setCode(404);
            customResponse.setMessage("视频不存在");
            return customResponse;
        }
        customResponse.setData(map);
        return customResponse;
    }

    @GetMapping("/video/user-works-count")
    public CustomResponse getUserWorksCount(@RequestParam("uid") Integer uid) {
        return new CustomResponse(200, "OK", redisUtil.zCard("user_video_upload:" + uid));
    }

    @GetMapping("/video/user-works")
    public CustomResponse getUserWorks(@RequestParam("uid") Integer uid,
                                       @RequestParam("rule") Integer rule,
                                       @RequestParam("page") Integer page,
                                       @RequestParam("quantity") Integer quantity) {
        CustomResponse customResponse = new CustomResponse();
        Map<String, Object> map = new HashMap<>();
        Set<Object> set = redisUtil.zReverange("user_video_upload:" + uid, 0, -1);
        if (set == null || set.isEmpty()) {
            map.put("count", 0);
            map.put("list", Collections.emptyList());
            customResponse.setData(map);
            return customResponse;
        }
        List<Integer> list = new ArrayList<>();
        set.forEach(vid -> {
            list.add((Integer) vid);
        });
        map.put("count", set.size());
        switch (rule) {
            case 1:
                map.put("list", videoService.getVideosWithDataByIdsOrderByDesc(list, "upload_date", page, quantity));
                break;
            case 2:
                map.put("list", videoService.getVideosWithDataByIdsOrderByDesc(list, "play", page, quantity));
                break;
            case 3:
                map.put("list", videoService.getVideosWithDataByIdsOrderByDesc(list, "good", page, quantity));
                break;
            default:
                map.put("list", videoService.getVideosWithDataByIdsOrderByDesc(list, "upload_date", page, quantity));
        }
        customResponse.setData(map);
        return customResponse;
    }

    @GetMapping("/video/user-love")
    public CustomResponse getUserLoveMovies(@RequestParam("uid") Integer uid,
                                            @RequestParam("offset") Integer offset,
                                            @RequestParam("quantity") Integer quantity) {
        CustomResponse customResponse = new CustomResponse();
        Set<Object> set = redisUtil.zReverange("love_video:" + uid, (long) offset, (long) offset + quantity - 1);
        if (set == null || set.isEmpty()) {
            customResponse.setData(Collections.emptyList());
            return customResponse;
        }
        List<Integer> list = new ArrayList<>();
        set.forEach(vid -> {
            list.add((Integer) vid);
        });
        customResponse.setData(videoService.getVideosWithDataByIdsOrderByDesc(list, null, 1, list.size()));
        return customResponse;
    }

    @GetMapping("/video/user-play")
    public CustomResponse getUserPlayMovies(@RequestParam("offset") Integer offset,
                                            @RequestParam("quantity") Integer quantity) {
        Integer uid = currentUser.getUserId();
        CustomResponse customResponse = new CustomResponse();
        Set<Object> set = redisUtil.zReverange("user_video_history:" + uid, (long) offset, (long) offset + quantity - 1);
        if (set == null || set.isEmpty()) {
            customResponse.setData(Collections.emptyList());
            return customResponse;
        }
        List<Integer> list = new ArrayList<>();
        set.forEach(vid -> {
            list.add((Integer) vid);
        });
        customResponse.setData(videoService.getVideosWithDataAndUidByIdsOrderByDesc(list, null, 1, list.size(), uid));
        return customResponse;
    }


    @GetMapping("/video/user-collect")
    public CustomResponse getUserCollectVideos(@RequestParam("fid") Integer fid,
                                               @RequestParam("rule") Integer rule,
                                               @RequestParam("page") Integer page,
                                               @RequestParam("quantity") Integer quantity) {
        CustomResponse customResponse = new CustomResponse();
        Set<Object> set;
        if (rule == 1) {
            set = redisUtil.zReverange("favorite_video:" + fid, (long) (page - 1) * quantity, (long) page * quantity);
        } else {
            set = redisUtil.zReverange("favorite_video:" + fid, 0, -1);
        }
        if (set == null || set.isEmpty()) {
            customResponse.setData(Collections.emptyList());
            return customResponse;
        }
        List<Integer> list = new ArrayList<>();
        set.forEach(vid -> {
            list.add((Integer) vid);
        });
        List<Map<String, Object>> result;
        switch (rule) {
            case 1:
                result = videoService.getVideosWithDataByIdsOrderByDesc(list, null, page, quantity);
                break;
            case 2:
                result = videoService.getVideosWithDataByIdsOrderByDesc(list, "play", page, quantity);
                break;
            case 3:
                result = videoService.getVideosWithDataByIdsOrderByDesc(list, "upload_date", page, quantity);
                break;
            default:
                result = videoService.getVideosWithDataByIdsOrderByDesc(list, null, page, quantity);
        }
        if (result.size() == 0) {
            customResponse.setData(result);
            return customResponse;
        }
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            result.stream().parallel().forEach(map -> {
                Video video = (Video) map.get("video");
                QueryWrapper<FavoriteVideo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("vid", video.getVid()).eq("fid", fid);
                map.put("info", favoriteVideoMapper.selectOne(queryWrapper));
            });
            sqlSession.commit();
        }
        customResponse.setData(result);
        return customResponse;
    }

    @PostMapping(value = "/video/update/{vid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CustomResponse updateVideoInfo(@PathVariable Integer vid,
                                          @RequestParam("cover") MultipartFile cover,
                                          @RequestParam("title") String title,
                                          @RequestParam("type") Integer type,
                                          @RequestParam("auth") Integer auth,
                                          @RequestParam("mcid") String mcid,
                                          @RequestParam("scid") String scid,
                                          @RequestParam("tags") String tags,
                                          @RequestParam("descr") String descr,
                                          @RequestParam("status") Integer status
                                          ) {
        Video videoInfo = new Video(null, null, title, type, auth, null, mcid, scid, tags, descr, null, null, status, null, null);

        return videoService.updateVideoInfo(vid, videoInfo, cover);
    }

    @GetMapping("/video/daily-play-count")
    public CustomResponse getDailyPlayCount(@RequestParam Integer uid) {
        LocalDate startDate = LocalDate.now().minusDays(7);
        QueryWrapper<DailyUserPlayCount> queryWrapper = Wrappers.query();
        queryWrapper.select("date", "SUM(play_count) as total_play_count")
                .eq("uid", uid)
                .ge("date", startDate)
                .groupBy("date");

        List<Map<String, Object>> playCounts = dailyUserPlayCountMapper.selectMaps(queryWrapper);
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(playCounts);
        return customResponse;
    }

    @GetMapping("/video/latest")
    public CustomResponse getLatestVideo() {

        CustomResponse customResponse = new CustomResponse();

        try {
            List<Video> videos = videoService.getAllVideosOrderByUploadDate();
            customResponse.setData(videos);
            customResponse.setCode(200);
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("获取最新视频失败");
        }

        return customResponse;

    }

    @GetMapping("/video/uploaded-last-7-days")
    public CustomResponse getDailyVideoUploadCount() {

        CustomResponse customResponse = new CustomResponse();

        try {
            List<Map<String, Object>> dailyVideoUploadCount = videoService.getDailyVideoUploadCount();
            customResponse.setData(dailyVideoUploadCount);
            customResponse.setCode(200);
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("获取最近7天视频上传量失败");
        }

        return customResponse;
    }

    @GetMapping("/video/summary")
    public CustomResponse getSummary() {

        CustomResponse customResponse = new CustomResponse();

        try {
            Map<String, Object> summary = videoService.getSummary();
            customResponse.setData(summary);
            customResponse.setCode(200);
        } catch (Exception e) {
            e.printStackTrace();
            customResponse.setCode(500);
            customResponse.setMessage("获取视频统计信息失败");
        }

        return customResponse;
    }

    @GetMapping("/video/get-all")
    public CustomResponse getAllVideosWithPagination(@RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "quantity", defaultValue = "10") Integer quantity){
        CustomResponse customResponse = new CustomResponse();
        Integer status = 1;

        Set<Object> set = redisUtil.getMembers("video_status:" + status);

        if (set != null && set.size() != 0) {
            List<Map<String, Object>> mapList = videoService.getVideosWithDataByIds(set, page, quantity);
            customResponse.setData(mapList);
        }
        return customResponse;

    }

    @GetMapping("/video/data/get-by-vids")
    public CustomResponse getVideoByUid(@RequestParam("uid") Integer uid) {
        CustomResponse customResponse = new CustomResponse();

        List<Integer> videoIds = videoService.getVideoIdsByUid(uid);
        Map<String, Object> map = new HashMap<>();

        if (videoIds.size() == 0) {
            map.put("count", 0);
            map.put("list", Collections.emptyList());
            customResponse.setData(map);
            return customResponse;
        }

        map.put("count", videoIds.size());
        map.put("list", videoService.getVideosWithDataByIdList(videoIds));

        customResponse.setData(map);
        return customResponse;

    }

    @GetMapping("/video/user-recent-coins")
    public CustomResponse getUserRecentCoinedVideos(@RequestParam("uid") Integer uid,
                                                    @RequestParam("offset") Integer offset,
                                                    @RequestParam("quantity") Integer quantity) {
        CustomResponse customResponse = new CustomResponse();
        List<Integer> videoIds = userVideoService.getRecentCoinedVideos(uid, offset, quantity);
        if (videoIds.isEmpty()) {
            customResponse.setData(Collections.emptyList());
            return customResponse;
        }
        customResponse.setData(videoService.getVideosWithDataByIdsOrderByDesc(videoIds, null, 1, quantity));
        return customResponse;
    }

    @GetMapping("/video/get-by-main-category")
    public CustomResponse getVideosByMainCategory(@RequestParam("mcId") String mcId, @RequestParam("scId") String scId){

        List<Integer> videoIds = videoService.getVideosByMainCategory(mcId, scId);
        Collections.shuffle(videoIds); // 随机打乱视频 ID 列表
        List<Integer> randomVideoIds = videoIds.stream().limit(10).collect(Collectors.toList()); // 获取前 10 个视频 ID
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(videoService.getVideosWithDataByIdList(randomVideoIds));
        return customResponse;
    }

    @GetMapping("/video/score")
    public CustomResponse getVideoScore() {
        List<Map<String, Object>> top100Scores = videoService.getVideoScore();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(top100Scores);
        return customResponse;
    }

    @PostMapping("/video/update-score")
    public CustomResponse updateVideoScore(@RequestParam("vid") Integer vid, @RequestParam("newScore") Double newScore) {
        videoService.updateVideoScore(vid, newScore);
        return new CustomResponse(200, "视频评分已更新", null);
    }


    @PostMapping("/video/delete-score")
    public CustomResponse deleteVideoScore(@RequestParam("vid") Integer vid) {
        videoService.deleteVideoScore(vid);
        return new CustomResponse(200, "视频评分已删除", null);
    }

    @GetMapping("/video/details-by-vids")
    public CustomResponse getVideoDetailsByVids(@RequestParam("vids") List<Integer> vids,
                                                @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                @RequestParam(value = "quantity", defaultValue = "10") Integer quantity) {
        CustomResponse customResponse = new CustomResponse();
        List<Map<String, Object>> videoDetails = videoService.getVideosWithDataByIdsOrderByDesc(vids, null, page, quantity);
        customResponse.setData(videoDetails);
        return customResponse;
    }

    @GetMapping("/video/category-counts")
    public CustomResponse getCategoryVideoCounts() {
        List<Map<String, Object>> categoryVideoCounts = videoService.getCategoryVideoCounts();
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(categoryVideoCounts);
        return customResponse;
    }

    @GetMapping("/video/ranked-details")
    public CustomResponse getRankedVideosWithDetails(@RequestParam(value = "start", defaultValue = "0") Integer start,
                                                    @RequestParam(value = "end", defaultValue = "9") Integer end) {
        List<Map<String, Object>> rankedVideosWithDetails = videoService.getRankedVideosWithDetails(start, end);
        CustomResponse customResponse = new CustomResponse();
        customResponse.setData(rankedVideosWithDetails);
        return customResponse;
    }
}
