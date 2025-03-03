package com.video.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyUserPlayCount {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer uid;

    private Integer playCount;

    private LocalDate date;
}
