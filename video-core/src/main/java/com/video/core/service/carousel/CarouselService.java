package com.video.core.service.carousel;

import org.springframework.web.multipart.MultipartFile;

import com.video.core.entity.CustomResponse;

public interface CarouselService {

    CustomResponse getCarouseInfo();

    CustomResponse addCarousel(MultipartFile img, String title, String color, String target);

    CustomResponse updateCarousel(Integer id, String imgUrl, String title, String color, String target);

    CustomResponse deleteCarousel(Integer id);
}
