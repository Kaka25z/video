<template>
    <div class="system-info">
        <el-card class="info-card">
            <el-row :gutter="20">
                <el-col :span="12">
                    <el-card class="inner-card" shadow="never">
                        <h3>操作系统信息</h3>
                        <el-table :data="osInfo" style="width: 100%">
                            <el-table-column prop="key" label="属性"></el-table-column>
                            <el-table-column prop="value" label="值"></el-table-column>
                        </el-table>
                    </el-card>
                </el-col>
                <el-col :span="12">
                    <el-card class="inner-card" shadow="never">
                        <h3>虚拟机信息</h3>
                        <el-table :data="vmInfo" style="width: 100%">
                            <el-table-column prop="key" label="属性"></el-table-column>
                            <el-table-column prop="value" label="值"></el-table-column>
                        </el-table>
                    </el-card>
                </el-col>
            </el-row>
        </el-card>

        <el-card class="info-card">
            <div id="memory-chart" style="width: 100%; height: 400px;"></div>
        </el-card>

        <el-card class="info-card">
            <h2>垃圾收集器信息</h2>
            <el-row :gutter="20">
                <el-col :span="12">
                    <el-card class="inner-card" shadow="never">
                        <h3>PS MarkSweep</h3>
                        <p>次数: {{ systemInfo["垃圾收集器名称 PS MarkSweep"] }}</p>
                        <p>累计收集时间: {{ systemInfo["垃圾收集器累计收集时间 PS MarkSweep"] }} ms</p>
                    </el-card>
                </el-col>
                <el-col :span="12">
                    <el-card class="inner-card" shadow="never">
                        <h3>PS Scavenge</h3>
                        <p>次数: {{ systemInfo["垃圾收集器名称 PS Scavenge"] }}</p>
                        <p>累计收集时间: {{ systemInfo["垃圾收集器累计收集时间 PS Scavenge"] }} ms</p>
                    </el-card>
                </el-col>
            </el-row>
        </el-card>

        <el-card class="info-card">
            <h2>其他信息</h2>
            <el-table :data="otherInfo" style="width: 100%">
                <el-table-column prop="key" label="属性"></el-table-column>
                <el-table-column prop="value" label="值"></el-table-column>
            </el-table>
        </el-card>
    </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
    name: 'SystemInfo',
    data() {
        return {
            systemInfo: {},
            osInfo: [],
            vmInfo: [],
            otherInfo: []
        };
    },
    methods: {
        async fetchSystemInfo() {
            const res = await this.$get('/system/info', {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token"),
                },
            });
            if (res.data.data) {
                this.systemInfo = res.data.data;
                this.processSystemInfo();
                this.initMemoryChart();
            }
        },
        processSystemInfo() {
            this.osInfo = [
                { key: '操作系统名称', value: this.systemInfo["操作系统名称"] },
                { key: '操作系统版本', value: this.systemInfo["操作系统版本"] },
                { key: '操作系统架构', value: this.systemInfo["操作系统架构"] }
            ];

            this.vmInfo = [
                { key: '虚拟机名称', value: this.systemInfo["虚拟机名称"] },
                { key: '虚拟机版本', value: this.systemInfo["虚拟机版本"] },
                { key: '虚拟机供应商', value: this.systemInfo["虚拟机供应商"] }
            ];

            this.otherInfo = [
                { key: '空闲内存', value: this.systemInfo["空闲内存"] },
                { key: '启动时间', value: this.systemInfo["启动时间"] },
                { key: '可用处理器数量', value: this.systemInfo["可用处理器数量"] },
                { key: '类路径', value: this.systemInfo["类路径"] },
                { key: '总启动线程数量', value: this.systemInfo["总启动线程数量"] },
                { key: '运行时间', value: this.systemInfo["运行时间"] },
                { key: '总编译时间', value: this.systemInfo["总编译时间"] },
                { key: '总内存', value: this.systemInfo["总内存"] },
                { key: '峰值线程数量', value: this.systemInfo["峰值线程数量"] },
                { key: '守护线程数量', value: this.systemInfo["守护线程数量"] },
                { key: 'JIT编译器名称', value: this.systemInfo["JIT编译器名称"] },
                { key: '最大内存', value: this.systemInfo["最大内存"] },
                { key: '线程数量', value: this.systemInfo["线程数量"] }
            ].filter(item => item.value >= 0);
        },
        initMemoryChart() {
            const memoryChart = echarts.init(document.getElementById('memory-chart'));
            const option = {
                title: {
                    text: '内存使用情况'
                },
                tooltip: {
                    trigger: 'axis'
                },
                legend: {
                    data: ['已使用', '已提交', '最大']
                },
                xAxis: {
                    type: 'category',
                    data: ['堆内存', '非堆内存', 'PS Eden Space', 'PS Survivor Space', 'PS Old Gen', 'Metaspace', 'Code Cache', 'Compressed Class Space'],
                    axisLabel: {
                        interval: 0,
                        rotate: 30
                    }
                },
                yAxis: {
                    type: 'value'
                },
                series: [
                    {
                        name: '已使用',
                        type: 'bar',
                        data: [
                            this.systemInfo["堆内存使用情况"].used,
                            this.systemInfo["非堆内存使用情况"].used,
                            this.systemInfo["内存池名称 PS Eden Space"].used,
                            this.systemInfo["内存池名称 PS Survivor Space"].used,
                            this.systemInfo["内存池名称 PS Old Gen"].used,
                            this.systemInfo["内存池名称 Metaspace"].used,
                            this.systemInfo["内存池名称 Code Cache"].used,
                            this.systemInfo["内存池名称 Compressed Class Space"].used
                        ].filter(value => value >= 0)
                    },
                    {
                        name: '已提交',
                        type: 'bar',
                        data: [
                            this.systemInfo["堆内存使用情况"].committed,
                            this.systemInfo["非堆内存使用情况"].committed,
                            this.systemInfo["内存池名称 PS Eden Space"].committed,
                            this.systemInfo["内存池名称 PS Survivor Space"].committed,
                            this.systemInfo["内存池名称 PS Old Gen"].committed,
                            this.systemInfo["内存池名称 Metaspace"].committed,
                            this.systemInfo["内存池名称 Code Cache"].committed,
                            this.systemInfo["内存池名称 Compressed Class Space"].committed
                        ].filter(value => value >= 0)
                    },
                    {
                        name: '最大',
                        type: 'bar',
                        data: [
                            this.systemInfo["堆内存使用情况"].max,
                            this.systemInfo["非堆内存使用情况"].max,
                            this.systemInfo["内存池名称 PS Eden Space"].max,
                            this.systemInfo["内存池名称 PS Survivor Space"].max,
                            this.systemInfo["内存池名称 PS Old Gen"].max,
                            this.systemInfo["内存池名称 Metaspace"].max,
                            this.systemInfo["内存池名称 Code Cache"].max,
                            this.systemInfo["内存池名称 Compressed Class Space"].max
                        ].filter(value => value >= 0)
                    }
                ]
            };
            memoryChart.setOption(option);
        }
    },
    async created() {
        await this.fetchSystemInfo();
    }
};
</script>

<style scoped>
.system-info {
    padding: 20px;
}

.info-card {
    margin-bottom: 20px;
    padding: 15px;
    border-radius: 15px;
}

.inner-card {
    padding: 15px;
    border-radius: 15px;
}

h2 {
    margin-bottom: 20px;
}

h3 {
    margin-bottom: 10px;
}
</style>