package com.video.core.service.impl.carousel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.video.core.entity.Carousel;
import com.video.core.entity.CustomResponse;
import com.video.core.mapper.CarouselMapper;
import com.video.core.service.carousel.CarouselService;
import com.video.core.utils.MinioUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CarouselServiceImpl implements CarouselService{

    @Autowired
    private CarouselMapper carouselMapper;

    @Autowired
    private MinioUtils minioUtils;

    @Override
    public CustomResponse getCarouseInfo() {
        CustomResponse customResponse = new CustomResponse();

        try {
            List<Carousel> carousels = carouselMapper.selectList(null);
            customResponse.setData(carousels);
            customResponse.setCode(200);
            customResponse.setMessage("获取成功");
        } catch (Exception e) {
            log.error("获取轮播图信息失败", e);
            customResponse.setCode(500);
            customResponse.setMessage("获取失败");
        }

        return customResponse;
    }

    @Override
    public CustomResponse addCarousel(MultipartFile img, String title, String color, String target) {
        CustomResponse customResponse = new CustomResponse();

        try {

            String imgUrl = minioUtils.uploadImage(img, "carousel");

            Carousel carousel = new Carousel();
            carousel.setUrl(imgUrl);
            carousel.setTitle(title);
            carousel.setColor(color);
            carousel.setTarget(target);

            carouselMapper.insert(carousel);

            customResponse.setCode(200);
            customResponse.setMessage("添加成功");
        } catch (Exception e) {
            log.error("添加轮播图失败", e);
            customResponse.setCode(500);
            customResponse.setMessage("添加失败");
        }

        return customResponse;
    }

    @Override
    public CustomResponse updateCarousel(Integer id, String imgUrl, String title, String color, String target) {
        CustomResponse customResponse = new CustomResponse();

        try {

            Carousel carousel = new Carousel();
            carousel.setId(id);
            carousel.setUrl(imgUrl);
            carousel.setTitle(title);
            carousel.setColor(color);
            carousel.setTarget(target);

            carouselMapper.updateById(carousel);

            customResponse.setCode(200);
            customResponse.setMessage("更新成功");
        } catch (Exception e) {
            log.error("更新轮播图失败", e);
            customResponse.setCode(500);
            customResponse.setMessage("更新失败");
        }

        return customResponse;
    }

    @Override
    public CustomResponse deleteCarousel(Integer id) {
        CustomResponse customResponse = new CustomResponse();

        try {
            carouselMapper.deleteById(id);

            customResponse.setCode(200);
            customResponse.setMessage("删除成功");
        } catch (Exception e) {
            log.error("删除轮播图失败", e);
            customResponse.setCode(500);
            customResponse.setMessage("删除失败");
        }

        return customResponse;
    }

    
}
