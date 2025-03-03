package com.video.core.service.impl.category;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.video.core.entity.Category;
import com.video.core.entity.CustomResponse;
import com.video.core.entity.dto.CategoryDTO;
import com.video.core.mapper.CategoryMapper;
import com.video.core.service.category.CategoryService;
import com.video.core.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    /**
     * 获取全部分区数据
     * @return CustomResponse对象
     */
    @Override
    public CustomResponse getAll() {
        CustomResponse customResponse = new CustomResponse();
        List<CategoryDTO> sortedCategories = new ArrayList<>();

        // 尝试从redis中获取数据
        try {
            sortedCategories = redisUtil.getAllList("categoryList", CategoryDTO.class);
            if (sortedCategories.size() != 0) {
                customResponse.setData(sortedCategories);
                return customResponse;
            }
            log.warn("redis中获取不到分区数据");
        } catch (Exception e) {
            log.error("获取redis分区数据失败");
        }

        // 将分区表一次全部查询出来，再在内存执行处理逻辑，可以减少数据库的IO
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        List<Category> list = categoryMapper.selectList(queryWrapper);

        // 开一个临时整合map
        Map<String, CategoryDTO> categoryDTOMap = new HashMap<>();

        for (Category category : list) {
            String mcId = category.getMcId();
            String scId = category.getScId();
            String mcName = category.getMcName();
            String scName = category.getScName();
            String descr = category.getDescr();
            List<String> rcmTag = new ArrayList<>();
            if (category.getRcmTag() != null) {
                String[] strings = category.getRcmTag().split("\n");    // 将每个标签切出来组成列表封装
                rcmTag = Arrays.asList(strings);
            }

            // 先将主分类和空的子分类列表整合到map中
            if (!categoryDTOMap.containsKey(mcId)) {
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setMcId(mcId);
                categoryDTO.setMcName(mcName);
                categoryDTO.setScList(new ArrayList<>());
                categoryDTOMap.put(mcId, categoryDTO);
            }

            // 把子分类整合到map的子分类列表里
            Map<String, Object> scMap = new HashMap<>();
            scMap.put("mcId", mcId);
            scMap.put("scId", scId);
            scMap.put("scName", scName);
            scMap.put("descr", descr);
            scMap.put("rcmTag", rcmTag);
            categoryDTOMap.get(mcId).getScList().add(scMap);

        }

        // 按指定序列排序
        List<String> sortOrder = Arrays.asList("anime", "guochuang", "douga", "game", "kichiku",
                "music", "dance", "cinephile", "ent", "knowledge",
                "tech", "information", "food", "life", "car",
                "fashion", "sports", "animal", "virtual");

        for (String mcId : sortOrder) {
            if (categoryDTOMap.containsKey(mcId)) {
                sortedCategories.add(categoryDTOMap.get(mcId));
            }
        }
        // 将分类添加到redis缓存中
        try {
            redisUtil.delValue("categoryList");
            redisUtil.setAllList("categoryList", sortedCategories);
        } catch (Exception e) {
            log.error("存储redis分类列表失败");
        }
        customResponse.setData(sortedCategories);
        return customResponse;
    }

    /**
     * 根据id查询对应分区信息
     * @param mcId 主分区ID
     * @param scId 子分区ID
     * @return Category类信息
     */
    @Override
    public Category getCategoryById(String mcId, String scId) {
        // 从redis中获取最新数据
        Category category = redisUtil.getObject("category:" + mcId + ":" + scId, Category.class);
        // 如果redis中没有数据，就从mysql中获取并更新到redis
        if (category == null) {
            QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mc_id", mcId).eq("sc_id", scId);
            category = categoryMapper.selectOne(queryWrapper);
            if (category == null) {
                return new Category();    // 如果不存在则返回空
            }

            Category finalCategory = category;
            CompletableFuture.runAsync(() -> {
                redisUtil.setExObjectValue("category:" + mcId + ":" + scId, finalCategory);  // 默认存活1小时
            }, taskExecutor);
        }
        return category;
    }

    @Override
    public CustomResponse addRcmTag(String mcId, String scId, String rcmTag) {

        CustomResponse response = new CustomResponse();

        try {
            Category category = getCategoryById(mcId, scId);
            if (category != null) {
                String currentRcmTag = category.getRcmTag();
                List<String> tagList = new ArrayList<>(Arrays.asList(currentRcmTag.split("\n")));
                if (!tagList.contains(rcmTag)) {
                    tagList.add(rcmTag);
                    category.setRcmTag(String.join("\n", tagList));
    
                    UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("mc_id", mcId).eq("sc_id", scId);
                    categoryMapper.update(category, updateWrapper);
    
                    CompletableFuture.runAsync(() -> {
                        redisUtil.delValue("categoryList");
                        redisUtil.delValue("category:" + mcId + ":" + scId);
                        redisUtil.setExObjectValue("category:" + mcId + ":" + scId, category);
                    }, taskExecutor);

                    response.setCode(200);
                    response.setMessage("添加成功");
                } else {
                    response.setCode(400);
                    response.setMessage("标签已存在");
                }
            } else {
                response.setCode(400);
                response.setMessage("未找到对应分区");
                log.error("添加推荐标签失败，未找到对应分区");
            }
        } catch (Exception e) { 
            response.setCode(500);
            response.setMessage("添加失败");
            log.error("添加推荐标签失败：" + e);
        }

        return response;
    }

    @Override
    public CustomResponse removeRcmTag(String mcId, String scId, String rcmTag) {

        CustomResponse response = new CustomResponse();

        try {
            Category category = getCategoryById(mcId, scId);
            if (category != null) {
                String[] tags = category.getRcmTag().split("\n");
                List<String> tagList = new ArrayList<>(Arrays.asList(tags));
                if (tagList.remove(rcmTag)) {
                    category.setRcmTag(String.join("\n", tagList));
    
                    UpdateWrapper<Category> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("mc_id", mcId).eq("sc_id", scId);
                    categoryMapper.update(category, updateWrapper);
    
                    CompletableFuture.runAsync(() -> {
                        redisUtil.delValue("categoryList");
                        redisUtil.delValue("category:" + mcId + ":" + scId);
                        redisUtil.setExObjectValue("category:" + mcId + ":" + scId, category);
                    }, taskExecutor);
    
                    response.setCode(200);
                    response.setMessage("标签删除成功");
                } else {
                    response.setCode(400);
                    response.setMessage("标签不存在");
                    log.error("删除标签失败，标签不存在");
                }
            } else {
                response.setCode(400);
                response.setMessage("未找到对应分区");
                log.error("删除标签失败，未找到对应分区");
            }
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("删除失败：" + e.getMessage());
            log.error("删除标签失败：" + e);
        }
    
        return response;
    }
}