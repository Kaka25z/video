package com.video.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.video.core.im.IMServer;

@SpringBootApplication
@EnableScheduling   // 启用定时任务
public class VideoCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoCoreApplication.class, args);

        new Thread(() -> {
            try {
                new IMServer().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
