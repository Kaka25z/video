package com.video.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.video.core.entity.CustomResponse;
import com.video.core.service.carousel.CarouselService;

@RestController
@RequestMapping("/carousel")
public class CarouseController {

    @Autowired
    private CarouselService carouselService;

    @GetMapping("/get-info")
    public CustomResponse getCarouselInfo() {
        return carouselService.getCarouseInfo();
    }

    @PostMapping("/add")
    public CustomResponse addCarousel(@RequestParam("img") MultipartFile img,
                                      @RequestParam("title") String title,
                                      @RequestParam("color") String color,
                                      @RequestParam("target") String target) {
        return carouselService.addCarousel(img, title, color, target);
    }

    @PostMapping("/update")
    public CustomResponse updateCarousel(@RequestParam("id") Integer id,
                                         @RequestParam("img") String img,
                                         @RequestParam("title") String title,
                                         @RequestParam("color") String color,
                                         @RequestParam("target") String target) {
        return carouselService.updateCarousel(id, img, title, color, target);
    }

    @PostMapping("/delete")
    public CustomResponse deleteCarousel(@RequestParam("id") Integer id) {
        return carouselService.deleteCarousel(id);
    }
}
