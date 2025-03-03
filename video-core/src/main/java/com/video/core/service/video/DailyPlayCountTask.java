package com.video.core.service.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.video.core.entity.DailyUserPlayCount;
import com.video.core.entity.User;
import com.video.core.mapper.DailyUserPlayCountMapper;
import com.video.core.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyPlayCountTask {

    @Autowired
    private DailyUserPlayCountMapper dailyUserPlayCountMapper;

    @Autowired
    private UserMapper userMapper;

    @Scheduled(cron = "0 0 0 */7 * ?")
    public void deleteOldestDayData() {
        LocalDate oldestDay = LocalDate.now().minusDays(7);
        dailyUserPlayCountMapper.delete(new QueryWrapper<DailyUserPlayCount>().lt("date", oldestDay));
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void createTodayDataForAllUsers() {
        LocalDate today = LocalDate.now();
        List<User> users = userMapper.selectList(null);

        for (User user : users) {
            DailyUserPlayCount todayData = new DailyUserPlayCount();
            todayData.setDate(today);
            todayData.setUid(user.getUid());
            todayData.setPlayCount(0);
            dailyUserPlayCountMapper.insert(todayData);
        }
    }
}
