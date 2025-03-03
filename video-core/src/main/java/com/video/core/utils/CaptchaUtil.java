package com.video.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.wf.captcha.GifCaptcha;
import com.wf.captcha.base.Captcha;

@Component
public class CaptchaUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public Map<String, Object> generateCaptcha() {

        GifCaptcha captcha = new GifCaptcha();

        captcha.setCharType(Captcha.TYPE_DEFAULT);
        captcha.setLen(5);

        String verifyCodeKey = UUID.randomUUID().toString();
        String verifyCode = captcha.text().toLowerCase();

        Map<String, Object> codeMap = new HashMap<>();
        codeMap.put("key", verifyCodeKey);
        codeMap.put("image", captcha.toBase64());

        redisTemplate.opsForValue().set(verifyCodeKey, verifyCode, 60, TimeUnit.SECONDS);

        return codeMap;
    }

    public Integer verifyCaptcha(String key, String code) {
        String verifyCode = redisTemplate.opsForValue().get(key);

        if (verifyCode == null) {
            return 1;
        }

        if (verifyCode.equals(code)) {
            redisTemplate.delete(key);
            return 0;
        } else {
            return 2;
        }
    }
}
