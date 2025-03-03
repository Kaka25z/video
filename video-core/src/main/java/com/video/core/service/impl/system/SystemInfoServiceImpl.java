package com.video.core.service.impl.system;

import com.video.core.service.system.SystemInfoService;
import org.springframework.stereotype.Service;

import java.lang.management.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class SystemInfoServiceImpl implements SystemInfoService {

    @Override
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        systemInfo.put("操作系统名称", osBean.getName());
        systemInfo.put("操作系统版本", osBean.getVersion());
        systemInfo.put("操作系统架构", osBean.getArch());
        systemInfo.put("可用处理器数量", osBean.getAvailableProcessors());

        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("总内存", runtime.totalMemory());
        systemInfo.put("空闲内存", runtime.freeMemory());
        systemInfo.put("最大内存", runtime.maxMemory());

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        systemInfo.put("堆内存使用情况", memoryMXBean.getHeapMemoryUsage());
        systemInfo.put("非堆内存使用情况", memoryMXBean.getNonHeapMemoryUsage());

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        systemInfo.put("运行时间", runtimeMXBean.getUptime());
        systemInfo.put("虚拟机名称", runtimeMXBean.getVmName());
        systemInfo.put("虚拟机供应商", runtimeMXBean.getVmVendor());
        systemInfo.put("虚拟机版本", runtimeMXBean.getVmVersion());
        systemInfo.put("启动时间", runtimeMXBean.getStartTime());
        systemInfo.put("类路径", runtimeMXBean.getClassPath());
        systemInfo.put("库路径", runtimeMXBean.getLibraryPath());
        systemInfo.put("系统属性", runtimeMXBean.getSystemProperties());

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        systemInfo.put("线程数量", threadMXBean.getThreadCount());
        systemInfo.put("峰值线程数量", threadMXBean.getPeakThreadCount());
        systemInfo.put("守护线程数量", threadMXBean.getDaemonThreadCount());
        systemInfo.put("总启动线程数量", threadMXBean.getTotalStartedThreadCount());

        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        if (compilationMXBean != null) {
            systemInfo.put("JIT编译器名称", compilationMXBean.getName());
            systemInfo.put("总编译时间", compilationMXBean.getTotalCompilationTime());
        }

        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            systemInfo.put("垃圾收集器名称 " + gcBean.getName(), gcBean.getCollectionCount());
            systemInfo.put("垃圾收集器累计收集时间 " + gcBean.getName(), gcBean.getCollectionTime());
        }

        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            systemInfo.put("内存池名称 " + pool.getName(), pool.getUsage());
        }

        return systemInfo;
    }
}