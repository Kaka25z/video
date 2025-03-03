package com.video.core.controller;

import com.video.core.entity.CustomResponse;
import com.video.core.service.system.SystemInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SystemInfoController {

    @Autowired
    private SystemInfoService systemInfoService;

    @GetMapping("/system/info")
    public CustomResponse getSystemInfo() {
        Map<String, Object> systemInfo = systemInfoService.getSystemInfo();
        return new CustomResponse(200, "系统信息获取成功", systemInfo);
    }
}