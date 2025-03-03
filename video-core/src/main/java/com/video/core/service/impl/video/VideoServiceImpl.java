package com.video.core.service.impl.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.video.core.entity.Category;
import com.video.core.entity.CustomResponse;
import com.video.core.entity.UserVideo;
import com.video.core.entity.Video;
import com.video.core.entity.VideoStats;
import com.video.core.mapper.CategoryMapper;
import com.video.core.mapper.UserMapper;
import com.video.core.mapper.UserVideoMapper;
import com.video.core.mapper.VideoMapper;
import com.video.core.mapper.VideoStatsMapper;
import com.video.core.service.category.CategoryService;
import com.video.core.service.user.UserService;
import com.video.core.service.utils.CurrentUser;
import com.video.core.service.video.UserVideoService;
import com.video.core.service.video.VideoService;
import com.video.core.service.video.VideoStatsService;
import com.video.core.utils.ESUtil;
import com.video.core.utils.MinioUtils;
import com.video.core.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserVideoService userVideoService;

    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MinioUtils ossUtil;

    @Autowired
    private ESUtil esUtil;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 根据id分页获取视频信息，包括用户和分区信息
     * @param set   要查询的视频id集合
     * @param index 分页页码 为空默认是1
     * @param quantity  每一页查询的数量 为空默认是10
     * @return  包含用户信息、分区信息、视频信息的map列表
     */
    @Override
    public List<Map<String, Object>> getVideosWithDataByIds(Set<Object> set, Integer index, Integer quantity) {
        if (index == null) {
            index = 1;
        }
        if (quantity == null) {
            quantity = 10;
        }
        int startIndex = (index - 1) * quantity;
        int endIndex = startIndex + quantity;
        // 检查数据是否足够满足分页查询
        if (startIndex > set.size()) {
            // 如果数据不足以填充当前分页，返回空列表
            return Collections.emptyList();
        }
        List<Video> videoList = new CopyOnWriteArrayList<>();   // 使用线程安全的集合类 CopyOnWriteArrayList 保证多线程处理共享List不会出现并发问题

        // 直接数据库分页查询    （平均耗时 13ms）
        List<Object> idList = new ArrayList<>(set);
        endIndex = Math.min(endIndex, idList.size());
        List<Object> sublist = idList.subList(startIndex, endIndex);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("vid", sublist).ne("status", 3);
        videoList = videoMapper.selectList(queryWrapper);
        if (videoList.isEmpty()) return Collections.emptyList();

        // 并行处理每一个视频，提高效率
        // 先将videoList转换为Stream
        Stream<Video> videoStream = videoList.stream();
        List<Map<String, Object>> mapList = videoStream.parallel() // 利用parallel()并行处理
                .map(video -> {
//                    long start = System.currentTimeMillis();
//                    System.out.println("================ 开始查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + start);
                    Map<String, Object> map = new HashMap<>();
                    map.put("video", video);

                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);

                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);

                    // 使用join()等待全部任务完成
                    userFuture.join();
                    categoryFuture.join();
//                    long end = System.currentTimeMillis();
//                    System.out.println("================ 结束查询 " + video.getVid() + " 号视频相关信息 ===============   当前时间 " + end + "   耗时 " + (end - start));

                    return map;
                })
                .collect(Collectors.toList());

//        end = System.currentTimeMillis();
//        System.out.println("封装耗时：" + (end - start));
        return mapList;
    }

    @Override
    public List<Map<String, Object>> getVideosWithDataByIdsOrderByDesc(List<Integer> idList, @Nullable String column, Integer page, Integer quantity) {
        // 使用事务批量操作 减少连接sql的开销
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<Map<String, Object>> result;
            if (column == null) {
                // 如果没有指定排序列，就按idList排序
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("vid", idList);
                List<Video> videos = videoMapper.selectList(queryWrapper);
                if (videos.isEmpty()) {
                    sqlSession.commit();
                    return Collections.emptyList();
                }
                result = idList.stream().parallel().flatMap(vid -> {
                    Map<String, Object> map = new HashMap<>();
                    // 找到videos中为vid的视频
                    Video video = videos.stream()
                            .filter(v -> Objects.equals(v.getVid(), vid))
                            .findFirst()
                            .orElse(null);
                    if (video == null) return Stream.empty(); // 跳过该项
                    if (video.getStatus() == 3) {
                        // 视频已删除
                        Video video1 = new Video();
                        video1.setVid(video.getVid());
                        video1.setUid(video.getUid());
                        video1.setStatus(video.getStatus());
                        video1.setDeleteDate(video.getDeleteDate());
                        map.put("video", video1);
                        return Stream.of(map);
                    }
                    map.put("video", video);
                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);
                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);
                    userFuture.join();
                    categoryFuture.join();
                    return Stream.of(map);
                }).collect(Collectors.toList());
            } else if (Objects.equals(column, "upload_date")) {
                // 如果按投稿日期排序，就先查video表
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("vid", idList).orderByDesc(column).last("LIMIT " + quantity + " OFFSET " + (page - 1) * quantity);
                List<Video> list = videoMapper.selectList(queryWrapper);
                if (list.isEmpty()) {
                    sqlSession.commit();
                    return Collections.emptyList();
                }
                result = list.stream().parallel().map(video -> {
                    Map<String, Object> map = new HashMap<>();
                    if (video.getStatus() == 3) {
                        // 视频已删除
                        Video video1 = new Video();
                        video1.setVid(video.getVid());
                        video1.setUid(video.getUid());
                        video1.setStatus(video.getStatus());
                        video1.setDeleteDate(video.getDeleteDate());
                        map.put("video", video1);
                        return map;
                    }
                    map.put("video", video);
                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);
                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);
                    userFuture.join();
                    categoryFuture.join();
                    return map;
                }).collect(Collectors.toList());
            } else {
                // 否则按视频数据排序，就先查数据
                QueryWrapper<VideoStats> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("vid", idList).orderByDesc(column).last("LIMIT " + quantity + " OFFSET " + (page - 1) * quantity);
                List<VideoStats> list = videoStatsMapper.selectList(queryWrapper);
                if (list.isEmpty()) {
                    sqlSession.commit();
                    return Collections.emptyList();
                }
                result = list.stream().parallel().map(videoStats -> {
                    Map<String, Object> map = new HashMap<>();
                    Video video = videoMapper.selectById(videoStats.getVid());
                    if (video.getStatus() == 3) {
                        // 视频已删除
                        Video video1 = new Video();
                        video1.setVid(video.getVid());
                        video1.setUid(video.getUid());
                        video1.setStatus(video.getStatus());
                        video1.setDeleteDate(video.getDeleteDate());
                        map.put("video", video1);
                        return map;
                    }
                    map.put("video", video);
                    map.put("stats", videoStats);
                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                    }, taskExecutor);
                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);
                    userFuture.join();
                    categoryFuture.join();
                    return map;
                }).collect(Collectors.toList());
            }
            sqlSession.commit();
            return result;
        }
    }

    /**
     * 根据vid查询单个视频信息，包含用户信息和分区信息
     * @param vid 视频ID
     * @return 包含用户信息、分区信息、视频信息的map
     */
    @Override
    public Map<String, Object> getVideoWithDataById(Integer vid) {
        Map<String, Object> map = new HashMap<>();
        // 先查询 redis
        Video video = redisUtil.getObject("video:" + vid, Video.class);
        if (video == null) {
            // redis 查不到再查数据库
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("vid", vid).ne("status", 3);
            video = videoMapper.selectOne(queryWrapper);
            if (video != null) {
                Video finalVideo1 = video;
                CompletableFuture.runAsync(() -> {
                    redisUtil.setExObjectValue("video:" + vid, finalVideo1);    // 异步更新到redis
                }, taskExecutor);
            } else  {
                return null;
            }
        }

        // 多线程异步并行查询用户信息和分区信息并封装
        Video finalVideo = video;
        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            map.put("user", userService.getUserById(finalVideo.getUid()));
            map.put("stats", videoStatsService.getVideoStatsById(finalVideo.getVid()));
        }, taskExecutor);
        CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
            map.put("category", categoryService.getCategoryById(finalVideo.getMcId(), finalVideo.getScId()));
        }, taskExecutor);
        map.put("video", video);
        // 使用join()等待userFuture和categoryFuture任务完成
        userFuture.join();
        categoryFuture.join();

        return map;
    }

    /**
     * 根据有序vid列表查询视频以及相关信息
     * @param list  vid有序列表
     * @return  有序的视频列表
     */
    @Override
    public List<Map<String, Object>> getVideosWithDataByIdList(List<Integer> list) {
        if (list.isEmpty()) return Collections.emptyList();
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("vid", list).ne("status", 3);
        List<Video> videos = videoMapper.selectList(queryWrapper);
        if (videos.isEmpty()) return Collections.emptyList();
        List<Map<String, Object>> mapList = list.stream().parallel().flatMap(
                vid -> {
                    Map<String, Object> map = new HashMap<>();
                    // 找到videos中为vid的视频
                    Video video = videos.stream()
                            .filter(v -> Objects.equals(v.getVid(), vid))
                            .findFirst()
                            .orElse(null);

                    if (video == null) {
                        return Stream.empty(); // 跳过该项
                    }
                    map.put("video", video);

                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);

                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);

                    userFuture.join();
                    categoryFuture.join();

                    return Stream.of(map);
                }
        ).collect(Collectors.toList());
        return mapList;
    }

    /**
     * 更新视频状态，包括过审、不通过、删除，其中审核相关需要管理员权限，删除可以是管理员或者投稿用户
     * @param vid   视频ID
     * @param status 要修改的状态，1通过 2不通过 3删除
     * @return 无data返回，仅返回响应信息
     */
    @Override
    @Transactional
    public CustomResponse updateVideoStatus(Integer vid, Integer status) throws IOException {
        CustomResponse customResponse = new CustomResponse();
        Integer userId = currentUser.getUserId();
        if (status == 1 || status == 2) {
            if (!currentUser.isAdmin()) {
                customResponse.setCode(403);
                customResponse.setMessage("您不是管理员，无权访问");
                return customResponse;
            }
            if (status == 1) {
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("vid", vid).ne("status", 3);
                Video video = videoMapper.selectOne(queryWrapper);
                if (video == null) {
                    customResponse.setCode(404);
                    customResponse.setMessage("视频不见了QAQ");
                    return customResponse;
                }
                Integer lastStatus = video.getStatus();
                video.setStatus(1);
                UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("vid", vid).set("status", 1).set("upload_date", new Date());     // 更新视频状态审核通过
                int flag = videoMapper.update(null, updateWrapper);
                if (flag > 0) {
                    // 更新成功
                    esUtil.updateVideo(video);  // 更新ES视频文档
                    redisUtil.delMember("video_status:" + lastStatus, vid);     // 从旧状态移除
                    redisUtil.addMember("video_status:1", vid);     // 加入新状态
                    redisUtil.zset("user_video_upload:" + video.getUid(), video.getVid());
                    redisUtil.delValue("video:" + vid);     // 删除旧的视频信息
                    return customResponse;
                } else {
                    // 更新失败，处理错误情况
                    customResponse.setCode(500);
                    customResponse.setMessage("更新状态失败");
                    return customResponse;
                }
            }
            else {
                // 目前逻辑跟上面一样的，但是可能以后要做一些如 记录不通过原因 等操作，所以就分开写了
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("vid", vid).ne("status", 3);
                Video video = videoMapper.selectOne(queryWrapper);
                if (video == null) {
                    customResponse.setCode(404);
                    customResponse.setMessage("视频不见了QAQ");
                    return customResponse;
                }
                Integer lastStatus = video.getStatus();
                video.setStatus(2);
                UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("vid", vid).set("status", 2);     // 更新视频状态审核不通过
                int flag = videoMapper.update(null, updateWrapper);
                if (flag > 0) {
                    // 更新成功
                    esUtil.updateVideo(video);  // 更新ES视频文档
                    redisUtil.delMember("video_status:" + lastStatus, vid);     // 从旧状态移除
                    redisUtil.addMember("video_status:2", vid);     // 加入新状态
                    redisUtil.zsetDelMember("user_video_upload:" + video.getUid(), video.getVid());
                    redisUtil.delValue("video:" + vid);     // 删除旧的视频信息
                    return customResponse;
                } else {
                    // 更新失败，处理错误情况
                    customResponse.setCode(500);
                    customResponse.setMessage("更新状态失败");
                    return customResponse;
                }
            }
        } else if (status == 3) {
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("vid", vid).ne("status", 3);
            Video video = videoMapper.selectOne(queryWrapper);
            if (video == null) {
                customResponse.setCode(404);
                customResponse.setMessage("视频不见了QAQ");
                return customResponse;
            }
            if (Objects.equals(userId, video.getUid()) || currentUser.isAdmin()) {
                String videoUrl = video.getVideoUrl();
                String videoPrefix = videoUrl.split("video/")[1];  // OSS视频文件名
                String coverUrl = video.getCoverUrl();
                String coverPrefix = coverUrl.split("video/")[1];  // OSS封面文件名
                Integer lastStatus = video.getStatus();
                UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("vid", vid).set("status", 3).set("delete_date", new Date());     // 更新视频状态已删除
                int flag = videoMapper.update(null, updateWrapper);
                if (flag > 0) {
                    // 更新成功
                    esUtil.deleteVideo(vid);
                    redisUtil.delMember("video_status:" + lastStatus, vid);     // 从旧状态移除
                    redisUtil.delValue("video:" + vid);     // 删除旧的视频信息
                    redisUtil.delValue("danmu_idset:" + vid);   // 删除该视频的弹幕
                    redisUtil.zsetDelMember("user_video_upload:" + video.getUid(), video.getVid());
                    // 搞个异步线程去删除OSS的源文件
                    CompletableFuture.runAsync(() -> ossUtil.deleteFiles(videoPrefix), taskExecutor);
                    CompletableFuture.runAsync(() -> ossUtil.deleteFiles(coverPrefix), taskExecutor);
                    // 批量删除该视频下的全部评论缓存
                    CompletableFuture.runAsync(() -> {
                        Set<Object> set = redisUtil.zReverange("comment_video:" + vid, 0, -1);
                        List<String> list = new ArrayList<>();
                        set.forEach(id -> list.add("comment_reply:" + id));
                        list.add("comment_video:" + vid);
                        redisUtil.delValues(list);
                    }, taskExecutor);
                    return customResponse;
                } else {
                    // 更新失败，处理错误情况
                    customResponse.setCode(500);
                    customResponse.setMessage("更新状态失败");
                    return customResponse;
                }
            } else {
                customResponse.setCode(403);
                customResponse.setMessage("您没有权限删除视频");
                return customResponse;
            }
        }
        customResponse.setCode(500);
        customResponse.setMessage("更新状态失败");
        return customResponse;
    }

    @Override
    @Transactional
    public CustomResponse updateVideoInfo(Integer vid, Video videoInfo, MultipartFile cover) {

        Video existingVideo = videoMapper.selectById(vid);
        if (existingVideo == null) {
            return new CustomResponse(404, "视频不存在", null);
        }

        String coverUrl = null;
        if (cover !=null && !cover.isEmpty()) {
            try {
                coverUrl = ossUtil.uploadImage(cover , "cover");
                videoInfo.setCoverUrl(coverUrl);
            } catch (IOException e) {
                log.error("封面上传失败", e);
                return new CustomResponse(500, "封面上传失败", null);
            }
        }

        BeanUtils.copyProperties(videoInfo, existingVideo, getNullPropertyNames(videoInfo));

        int updateResult = videoMapper.updateById(existingVideo);
        log.info("更新结果：" + updateResult);
        if (updateResult == 0) {
            return new CustomResponse(500, "更新失败", null);
        }

        CompletableFuture.runAsync(() -> {
            redisUtil.delValue("video:" + vid);
            redisUtil.setExObjectValue("video:" + vid, existingVideo);
        }, taskExecutor);

        return new CustomResponse();
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    @Override
    public List<Video> getAllVideosOrderByUploadDate() {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("upload_date").last("LIMIT 5");
        return videoMapper.selectList(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getDailyVideoUploadCount() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        LocalDate endDate = LocalDate.now();

        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(upload_date) as date", "COUNT(*) as total_uploads")
                    .ge("upload_date", startDate)
                    .groupBy("DATE(upload_date)");

        List<Map<String, Object>> uploadCounts = videoMapper.selectMaps(queryWrapper);

        Map<String, Integer> dateUploadMap = new HashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dateUploadMap.put(date.toString(), 0);
        }

        for (Map<String, Object> uploadCount : uploadCounts) {
            String date = uploadCount.get("date").toString();
            Integer totalUploads = Integer.parseInt(uploadCount.get("total_uploads").toString());
            dateUploadMap.put(date, totalUploads);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        dateUploadMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("date", entry.getKey());
                map.put("total_uploads", entry.getValue());
                result.add(map);
            });
    

        return result;
    }

    @Override
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        try {
            Long totalUsers = userMapper.selectCount(null);

            Long totalVideos = videoMapper.selectCount(null);

            List<Map<String, Object>> stats = videoStatsMapper.selectMaps(
                new QueryWrapper<VideoStats>().select(
                    "SUM(play) as totalPlay",
                    "SUM(coin) as totalCoin",
                    "SUM(comment) as totalComment",
                    "SUM(danmu) as totalDanmu"
                )
            );

            // 获取播放量前五的视频信息
            List<VideoStats> topVideos = videoStatsMapper.selectList(
                new QueryWrapper<VideoStats>().orderByDesc("play").last("LIMIT 5")
            );

            List<Map<String, Object>> result = new ArrayList<>();
            for (VideoStats videoStats : topVideos) {
                Map<String, Object> videoData = new HashMap<>();
                videoData.put("vid", videoStats.getVid());
                videoData.put("play", videoStats.getPlay());

                Video video = videoMapper.selectById(videoStats.getVid());
                if (video != null) {
                    videoData.put("title", video.getTitle());
                } else {
                    videoData.put("title", "未知标题");
                }
                result.add(videoData);
            }

            summary.put("totalUsers", totalUsers);
            summary.put("totalPlay", stats.get(0).get("totalPlay"));
            summary.put("totalCoin", stats.get(0).get("totalCoin"));
            summary.put("totalComment", stats.get(0).get("totalComment"));
            summary.put("totalDanmu", stats.get(0).get("totalDanmu"));
            summary.put("topVideos", result);
            summary.put("totalVideos", totalVideos);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取视频统计信息失败", e);
        }

        return summary;
    }

    @Override
    public List<Integer> getVideoIdsByUid(Integer uid) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).select("vid");
        List<Video> videos = videoMapper.selectList(queryWrapper);
        return videos.stream().map(Video::getVid).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getVideosWithDataAndUidByIdsOrderByDesc(List<Integer> idList, @Nullable String column, Integer page, Integer quantity, Integer uid) {
        // 使用事务批量操作 减少连接sql的开销
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<Map<String, Object>> result;
            if (column == null) {
                // 如果没有指定排序列，就按idList排序
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("vid", idList);
                List<Video> videos = videoMapper.selectList(queryWrapper);
                if (videos.isEmpty()) {
                    sqlSession.commit();
                    return Collections.emptyList();
                }
                result = idList.stream().parallel().flatMap(vid -> {
                    Map<String, Object> map = new HashMap<>();
                    // 找到videos中为vid的视频
                    Video video = videos.stream()
                            .filter(v -> Objects.equals(v.getVid(), vid))
                            .findFirst()
                            .orElse(null);
                    if (video == null) return Stream.empty(); // 跳过该项
                    if (video.getStatus() == 3) {
                        // 视频已删除
                        Video video1 = new Video();
                        video1.setVid(video.getVid());
                        video1.setUid(video.getUid());
                        video1.setStatus(video.getStatus());
                        video1.setDeleteDate(video.getDeleteDate());
                        map.put("video", video1);
                        return Stream.of(map);
                    }
                    map.put("video", video);
                    CompletableFuture<Void> userVideoFuture = CompletableFuture.runAsync(() -> {
                        map.put("user_video", userVideoService.getUserVideoByVid(video.getVid(), uid));
                    }, taskExecutor);
                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);
                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);
                    userVideoFuture.join();
                    userFuture.join();
                    categoryFuture.join();
                    return Stream.of(map);
                }).collect(Collectors.toList());
            } else if (Objects.equals(column, "upload_date")) {
                // 如果按投稿日期排序，就先查video表
                QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("vid", idList).orderByDesc(column).last("LIMIT " + quantity + " OFFSET " + (page - 1) * quantity);
                List<Video> list = videoMapper.selectList(queryWrapper);
                if (list.isEmpty()) {
                    sqlSession.commit();
                    return Collections.emptyList();
                }
                result = list.stream().parallel().map(video -> {
                    Map<String, Object> map = new HashMap<>();
                    if (video.getStatus() == 3) {
                        // 视频已删除
                        Video video1 = new Video();
                        video1.setVid(video.getVid());
                        video1.setUid(video.getUid());
                        video1.setStatus(video.getStatus());
                        video1.setDeleteDate(video.getDeleteDate());
                        map.put("video", video1);
                        return map;
                    }
                    map.put("video", video);
                    CompletableFuture<Void> userVideoFuture = CompletableFuture.runAsync(() -> {
                        map.put("user_video", userVideoService.getUserVideoByVid(video.getVid(), uid));
                    }, taskExecutor);
                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                        map.put("stats", videoStatsService.getVideoStatsById(video.getVid()));
                    }, taskExecutor);
                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);
                    userFuture.join();
                    userVideoFuture.join();
                    categoryFuture.join();
                    return map;
                }).collect(Collectors.toList());
            } else {
                // 否则按视频数据排序，就先查数据
                QueryWrapper<VideoStats> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("vid", idList).orderByDesc(column).last("LIMIT " + quantity + " OFFSET " + (page - 1) * quantity);
                List<VideoStats> list = videoStatsMapper.selectList(queryWrapper);
                if (list.isEmpty()) {
                    sqlSession.commit();
                    return Collections.emptyList();
                }
                result = list.stream().parallel().map(videoStats -> {
                    Map<String, Object> map = new HashMap<>();
                    Video video = videoMapper.selectById(videoStats.getVid());
                    if (video.getStatus() == 3) {
                        // 视频已删除
                        Video video1 = new Video();
                        video1.setVid(video.getVid());
                        video1.setUid(video.getUid());
                        video1.setStatus(video.getStatus());
                        video1.setDeleteDate(video.getDeleteDate());
                        map.put("video", video1);
                        return map;
                    }
                    map.put("video", video);
                    map.put("stats", videoStats);
                    CompletableFuture<Void> userVideoFuture = CompletableFuture.runAsync(() -> {
                        map.put("user_video", userVideoService.getUserVideoByVid(video.getVid(), uid));
                    }, taskExecutor);
                    CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
                        map.put("user", userService.getUserById(video.getUid()));
                    }, taskExecutor);
                    CompletableFuture<Void> categoryFuture = CompletableFuture.runAsync(() -> {
                        map.put("category", categoryService.getCategoryById(video.getMcId(), video.getScId()));
                    }, taskExecutor);
                    userVideoFuture.join();
                    userFuture.join();
                    categoryFuture.join();
                    return map;
                }).collect(Collectors.toList());
            }
            sqlSession.commit();
            return result;
        }
    }

    @Override
    public List<Integer> getVideosByMainCategory(String mcId, String scId) {
        // 使用 QueryWrapper 查询视频 ID 列表
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("vid").eq("mc_id", mcId).eq("sc_id", scId).eq("status", 1); // 假设 status 为 1 表示视频有效
        return videoMapper.selectList(queryWrapper).stream()
                          .map(Video::getVid)
                          .collect(Collectors.toList());
    }

    @Override
    public void calculateAndStoreVideoScore() {
        QueryWrapper<VideoStats> queryWrapper = new QueryWrapper<>();
        List<VideoStats> allVideos = videoStatsMapper.selectList(queryWrapper);
    
        for (VideoStats videoStats : allVideos) {
            double playScore = videoStats.getPlay() * 0.03;
            double danmuScore = videoStats.getDanmu() * 0.01;
            double goodScore = videoStats.getGood() * 0.02;
            double collectScore = videoStats.getCollect() * 0.015;
            double commentScore = videoStats.getComment() * 0.01;
            double coinScore = videoStats.getCoin() * 0.015;
    
            double rawScore = playScore + danmuScore + goodScore + collectScore + commentScore + coinScore;
    
            // 将分数按比例计算到1000万，并保留三位小数
            double score = (rawScore / 100000) * 100;
            score = Math.round(score * 1000.0) / 1000.0;
    
            redisUtil.zsetWithScore("video_scores", videoStats.getVid(), score);
        }
    }

    @Override
    public List<Map<String, Object>> getVideoScore() {
        List<RedisUtil.ZObjScore> top100Scores = redisUtil.zReverangeWithScores("video_scores", 0, 99);
        return top100Scores.stream()
                           .map(tuple -> {
                               Map<String, Object> scoreData = new HashMap<>();
                               scoreData.put("vid", tuple.getMember());
                               scoreData.put("score", tuple.getScore());
                               return scoreData;
                           })
                           .collect(Collectors.toList());
    }

    @Override
    public void updateVideoScore(Integer vid, Double newScore) {
        redisUtil.zsetWithScore("video_scores", vid, newScore);
    }

    @Override
    public void deleteVideoScore(Integer vid) {
        redisUtil.zsetDelMember("video_scores", vid);
    }

    @Override
    public List<Map<String, Object>> getCategoryVideoCounts() {
        List<Map<String, Object>> result = new ArrayList<>();
    
        // 查询所有子分区
        List<Category> subCategories = categoryMapper.selectList(new QueryWrapper<Category>()
            .select("DISTINCT sc_id", "sc_name", "mc_id", "mc_name"));
    
        // 按主分区分组
        Map<String, Map<String, Object>> mainCategoryMap = new HashMap<>();
        for (Category subCategory : subCategories) {
            String mcId = subCategory.getMcId();
            String mcName = subCategory.getMcName();
            String scId = subCategory.getScId();
            String scName = subCategory.getScName();
    
            // 如果主分区不存在，则创建
            if (!mainCategoryMap.containsKey(mcId)) {
                Map<String, Object> mainCategoryData = new HashMap<>();
                mainCategoryData.put("name", mcName);
    
                // 查询主分区下的视频总数
                long mainCategoryVideoCount = videoMapper.selectCount(new QueryWrapper<Video>().eq("mc_id", mcId));
                mainCategoryData.put("value", mainCategoryVideoCount);
    
                mainCategoryData.put("children", new ArrayList<Map<String, Object>>());
                mainCategoryMap.put(mcId, mainCategoryData);
            }
    
            // 查询子分区下的视频总数
            long subCategoryVideoCount = videoMapper.selectCount(new QueryWrapper<Video>().eq("sc_id", scId));
    
            // 添加子分区数据
            Map<String, Object> subCategoryData = new HashMap<>();
            subCategoryData.put("name", scName);
            subCategoryData.put("value", subCategoryVideoCount);
    
            List<Map<String, Object>> children = (List<Map<String, Object>>) mainCategoryMap.get(mcId).get("children");
            children.add(subCategoryData);
        }
    
        // 将主分区数据添加到结果中
        result.addAll(mainCategoryMap.values());
    
        result.removeIf(mainCategory -> {
            List<Map<String, Object>> children = (List<Map<String, Object>>) mainCategory.get("children");
            children.removeIf(child -> (long) child.get("value") == 0);
            return (long) mainCategory.get("value") == 0 && children.isEmpty();
        });

        return result;
    }

    @Override
    public List<Map<String, Object>> getRankedVideosWithDetails(Integer start, Integer end) {
        List<RedisUtil.ZObjScore> rankedVideos = redisUtil.zReverangeWithScores("video_scores", start, end);
        List<Integer> videoIds = rankedVideos.stream()
                                            .map(RedisUtil.ZObjScore::getMember)
                                            .map(Object::toString)
                                            .map(Integer::valueOf)
                                            .collect(Collectors.toList());
        List<Map<String, Object>> videoDetails = getVideosWithDataByIdsOrderByDesc(videoIds, null, 1, videoIds.size());
        for (int i = 0; i < videoDetails.size(); i++) {
            videoDetails.get(i).put("rank", start + i + 1);
        }
        return videoDetails;
    }
}


