package com.video.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.video.core.service.video.VideoService;

@Configuration
@EnableScheduling
public class SchedulerConfig implements ApplicationRunner{

    @Autowired
    private VideoService videoService;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void scheduleVideoScoreCalculation() {
        videoService.calculateAndStoreVideoScore();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        videoService.calculateAndStoreVideoScore();
    }
}
