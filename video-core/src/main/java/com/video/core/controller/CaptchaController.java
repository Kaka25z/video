package com.video.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.video.core.entity.CustomResponse;
import com.video.core.utils.CaptchaUtil;

@RestController
public class CaptchaController {


    @Autowired
    private CaptchaUtil captchaUtil;

    @GetMapping("/captcha/get")
    public CustomResponse getCaptcha() {

        CustomResponse response = new CustomResponse();
        response.setCode(200);
        response.setData(captchaUtil.generateCaptcha());
        response.setMessage("获取验证码成功");

        return response;
    }

    @PostMapping("/captcha/verify")
    public CustomResponse verifyCaptcha(@RequestParam("key") String key, @RequestParam("code") String code) {
        
        CustomResponse response = new CustomResponse();
        
        Integer result = captchaUtil.verifyCaptcha(key, code);

        if (result.equals(0)) {
            response.setCode(200);
            response.setMessage("验证码正确");
        } else if (result.equals(1)) {
            response.setCode(400);
            response.setMessage("验证码已过期");
        } else {
            response.setCode(400);
            response.setMessage("验证码错误");
        }

        return response;
    }
}
