package com.video.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.video.core.entity.CustomResponse;
import com.video.core.service.category.CategoryService;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取全部分区接口
     * @return CustomResponse对象
     */
    @GetMapping("/category/getall")
    public CustomResponse getAll() {
        return categoryService.getAll();
    }

    @PostMapping("/category/addTag")
    public CustomResponse addRcmTag(@RequestParam("mcId") String mcId,
                                    @RequestParam("scId") String scId,
                                    @RequestParam("rcmTag") String rcmTag) {
        return categoryService.addRcmTag(mcId, scId, rcmTag);
    }

    @PostMapping("/category/removeTag")
    public CustomResponse removeRcmTag(@RequestParam("mcId") String mcId,
                                       @RequestParam("scId") String scId,
                                       @RequestParam("rcmTag") String rcmTag) {
        return categoryService.removeRcmTag(mcId, scId, rcmTag);
    }

    @GetMapping("/category/get")
    public CustomResponse getCategoryById(@RequestParam("mcId") String mcId,
                                         @RequestParam("scId") String scId) {

        CustomResponse customResponse = new CustomResponse();
        try {
            customResponse.setData(categoryService.getCategoryById(mcId, scId));
            customResponse.setCode(200);
        } catch (Exception e) {
            customResponse.setCode(500);
            customResponse.setMessage("获取失败");
        }                     

        return customResponse;
    }
}
