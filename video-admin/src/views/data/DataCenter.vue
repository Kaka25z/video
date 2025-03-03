<script setup>
import { ref, computed, onMounted } from 'vue';
import { VueUiQuickChart, VueUiDonut, VueDataUi } from 'vue-data-ui';
import { handleNum } from '@/utils/utils';
import { get } from '@/network/request';
import { useStore } from 'vuex';

const store = useStore();

const totalData = ref({
    totalUsers: 0,
    totalVideos: 0,
    totalComment: 0,
    totalPlay: 0
});
const DailyUploadxAxisData = computed(() => dailyUpload.value.map(item => item.date));
const topVideosDataConfig = ref({
    style: {
        chart: {
            useGradient: false,
            title: {
                text: '热门视频播放量占比'
            },
            layout: {
                labels: {
                    dataLabels: {
                        show: false
                    },
                    hollow: {
                        average: {
                            show: false
                        },
                        total: {
                            text: '总播放量',
                        }
                    }
                }
            }
        }
    },
    userOptions: {
        show: false
    }
})

const TreemapConfig = ref({
    responsive: true,
    style: {
        chart: {
            height: 500,
            width: 1000,
            layout: {
                stored: {
                    show: false
                },
                labels: {
                    fontSize: 16,
                    prefix: "视频数量："
                },
                rects: {
                    gradient: {
                        show: false
                    }
                }
            },
            title: {
                text: "各分区视频数量"
            }
        }
    },
    userOptions: {
        show: false
    }
})

const dailyUploadConfig = ref({ "responsive": false, "backgroundColor": "#FFFFFF", "barAnimated": true, "barGap": 12, "barStrokeWidth": 1, "blurOnHover": true, "chartIsBarUnderDatasetLength": 6, "color": "#2D353C", "dataLabelFontSize": 14, "dataLabelRoundingPercentage": 1, "dataLabelRoundingValue": 1, "donutHideLabelUnderPercentage": 3, "donutLabelMarkerStrokeWidth": 1, "donutRadiusRatio": 0.4, "donutShowTotal": true, "donutStrokeWidth": 2, "donutThicknessRatio": 0.18, "donutTotalLabelFontSize": 24, "donutTotalLabelOffsetY": 0, "donutTotalLabelText": "Total", "donutUseShadow": false, "donutShadowColor": "#1A1A1A", "fontFamily": "inherit", "height": 338, "legendFontSize": 12, "legendIcon": "circleFill", "legendIconSize": 12, "lineAnimated": true, "lineSmooth": true, "lineStrokeWidth": 2, "paletteStartIndex": 0, "showDataLabels": true, "showLegend": true, "showTooltip": true, "showUserOptions": true, "userOptionsButtons": { "tooltip": true, "pdf": true, "img": true, "fullscreen": true, "annotator": true }, "userOptionsButtonTitles": { "open": "Open options", "close": "Close options", "tooltip": "Toggle tooltip", "pdf": "Download PDF", "img": "Download PNG", "fullscreen": "Toggle fullscreen", "annotator": "Toggle annotator" }, "title": "近7日投稿数量", "titleBold": true, "titleFontSize": 16, "titleTextAlign": "center", "tooltipCustomFormat": null, "tooltipBorderRadius": 4, "tooltipBorderColor": "#e1e5e8", "tooltipBorderWidth": 1, "tooltipFontSize": 14, "tooltipBackgroundOpacity": 30, "tooltipPosition": "center", "tooltipOffsetY": 24, "useCustomLegend": false, "valuePrefix": "", "valueSuffix": "", "width": 600, "xyAxisStroke": "#CCCCCC", "xyAxisStrokeWidth": 1, "xyGridStroke": "#e1e5e8", "xyGridStrokeWidth": 0.5, "xyHighlighterColor": "#000000", "xyHighlighterOpacity": 0.05, "xyLabelsXFontSize": 8, "xyLabelsYFontSize": 12, "xyPaddingBottom": 48, "xyPaddingLeft": 48, "xyPaddingRight": 12, "xyPaddingTop": 24, "xyPeriods": DailyUploadxAxisData, "xyPeriodLabelsRotation": 0, "xyScaleSegments": 10, "xyShowAxis": true, "xyShowGrid": true, "xyShowScale": true, "yAxisLabel": "投稿数量", "xAxisLabel": "日期", "axisLabelsFontSize": 12, "userOptionsPosition": "right", "zoomXy": true, "zoomColor": "#CCCCCC", "zoomHighlightColor": "#4A4A4A", "zoomFontSize": 14, "zoomUseResetSlot": false, "zoomMinimap": { "show": true, "smooth": true, "selectedColor": "#1f77b4", "selectedColorOpacity": 0.2, "lineColor": "#1f77b4", "selectionRadius": 2, "indicatorColor": "#1A1A1A" }, "zoomStartIndex": null, "zoomEndIndex": null, "showUserOptionsOnChartHover": false, "keepUserOptionsStateOnChartLeave": true });
const dailyUploadDataFake = ref([25, 40, 55, 31, 60, 78, 90]);
const dailyUpload = ref([]);
const dailyUploadData = ref([]);
const topVideos = ref([]);
const topVideosData = ref([]);
const TreemapDataset = ref([]);


const fetchData = async () => {
    const res = await get("/video/summary", {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token"),
        },
    });

    if (res.status === 200) {
        const summaryData = res.data.data;
        const topVideosList = summaryData.topVideos;
        const totalDataObj = Object.keys(summaryData)
            .filter(key => key !== 'topVideos')
            .reduce((obj, key) => {
                obj[key] = formatNumber(summaryData[key]);
                return obj;
            }, {});

        totalData.value = totalDataObj;
        topVideos.value = topVideosList;
        initTopVideosData();
    }
};

const getDailyUploadData = async () => {
    const res = await get("/video/uploaded-last-7-days", {
        headers: { Authorization: "Bearer " + localStorage.getItem("token"), }
    });

    if (res.status === 200) {
        dailyUpload.value = res.data.data;
        initDailyUploadData();
    }
}

const getTreemapData = async () => {
    await store.dispatch('getTreemapData');
    TreemapDataset.value = store.state.treemapData;
}

const formatNumber = (num) => {
    return handleNum(num);
};

const initTopVideosData = () => {
    topVideosData.value = topVideos.value.map((video, index) => ({
        name: video.title,
        color: getColor(index),
        values: [video.play],
    }));
};

const initDailyUploadData = () => {
    dailyUploadData.value = [dailyUpload.value.map(item => item.total_uploads)];
};

const getColor = (index) => {
    const colors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'];
    return colors[index % colors.length];
};

onMounted(() => {
    fetchData();
    getDailyUploadData();
    getTreemapData();
});
</script>

<template>
    <div class="flex-fill" style="background-color: rgb(250,250,250);">
        <div class="main">
            <el-row>
                <el-col :span="6">
                    <el-card class="col">
                        <div class="content">
                            <el-text class="eltext" type="warning">{{ totalData.totalUsers }}</el-text>
                            <el-text type="warning">用户总数</el-text>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card class="col">
                        <div class="content">
                            <el-text class="eltext" type="success">{{ totalData.totalVideos }}</el-text>
                            <el-text type="success">视频总数</el-text>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card class="col">
                        <div class="content">
                            <el-text class="eltext" type="danger">{{ totalData.totalComment }}</el-text>
                            <el-text type="danger">评论总数</el-text>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card class="col">
                        <div class="content">
                            <el-text class="eltext" type="info">{{ totalData.totalPlay }}</el-text>
                            <el-text type="info">播放总数</el-text>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="14">
                    <el-card class="col">
                        <VueUiQuickChart :dataset="dailyUploadDataFake" :config="dailyUploadConfig" />
                    </el-card>
                </el-col>
                <el-col :span="10">
                    <el-card class="col">
                        <VueUiDonut :dataset="topVideosData" :config="topVideosDataConfig" />
                    </el-card>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="24">
                    <el-card class="col">
                        <VueDataUi component="VueUiTreemap" :dataset="TreemapDataset" :config="TreemapConfig" />
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<!-- <template>
    <div class="flex-fill" style="background-color: rgb(250,250,250);">
        <div class="main">
            <el-row>
                <el-col :span=6 >
                    <el-card class="col">
                        <div class="content">
                            <el-text class="eltext" type="warning"> {{  this.totalData.totalUsers }} </el-text>
                            <el-text type="warning"> 用户总数 </el-text>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span=6 >
                    <el-card class="col">
                        <div class="content">
                            <el-text class="eltext" type="success"> {{  this.totalData.totalVideos }} </el-text>
                            <el-text type="success"> 视频总数 </el-text>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span=6 >
                    <el-card class="col">
                        <div class="content">
                            <el-text class="eltext" type="danger"> {{  this.totalData.totalComment }} </el-text>
                            <el-text type="danger"> 评论总数 </el-text>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span=6 >
                    <el-card class="col">
                        <div class="content">
                            <el-text class="eltext" type="info"> {{  this.totalData.totalPlay }} </el-text>
                            <el-text type="info"> 播放总数 </el-text>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span=12>
                    <el-card class="col">
                        <div class="pie">
                            <div id="dailyUpload" style="width: 100%; height: 400px"></div>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span=12>
                    <el-card class="col">
                        <h3>热门视频</h3>
                        <VueUiDonut :dataset="dataset" />
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>
</template> -->

<!-- <script >
import * as echarts from 'echarts';
import { handleNum } from '@/utils/utils';

export default {
    name: "DataCenter",
    data() {
        return {
            totalData:[],
            topVideos:[],
            daliyUpload:[],
            topVideosData: [],
            config: {},
            dailyUploadColumns: [
                { label: '日期', prop: 'date' },
                { label: '投稿数', prop: 'total_uploads' }
            ],
            topVideosColumns: [
                { label: '视频ID', prop: 'vid' },
                { label: '标题', prop: 'title' },
                { label: '播放次数', prop: 'views' }
            ]
        }
    },
    computed: {
        DailyUploadxAxisData() {
            return this.daliyUpload.map(item => item.date);
        },

        DailyUploadseriesData() {
            return this.daliyUpload.map(item => item.total_uploads);
        }
    },
    methods: {
        async fetchData() {
            const res = await this.$get("/video/summary", {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token"),
                },
            });

            if (res.data.code === 200) {
                const data = res.data.data;
                const topVideos = data.topVideos;
                const totalData = Object.keys(data)
                    .filter(key => key !== 'topVideos')
                    .reduce((obj, key) => {
                        obj[key] = this.formatNumber(data[key]);
                        return obj;
                    }, {});

                this.totalData = totalData;
                this.topVideos = topVideos;
                console.log(this.topVideos);
                this.initTopVideosData();
            }

            const response = await this.$get("/video/uploaded-last-7-days", {
                headers: { Authorization: "Bearer " + localStorage.getItem("token"), }
            });

            if (response.data.code === 200) {
                this.daliyUpload = response.data.data;
                console.log(this.daliyUpload);
                this.initDailyUpload();
            }
        },

        formatNumber(num) {
            return handleNum(num);
        },

        initTopVideosData() {
            this.topVideosData = this.topVideos.map((video, index) => ({
                name: `Series ${index + 1}`,
                color: this.getColor(index),
                values: [video.play]
            }));
            console.log(this.topVideosData);
        },

        getColor(index) {
            const colors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'];
            return colors[index % colors.length];
        },

        // initTopVideosChart() {
        //     const chartDom = document.getElementById('topVideos');
        //     const myChart = echarts.init(chartDom);
        //     const option = {
        //         title: {
        //             text: '热门视频播放量占比'
        //         },
        //         tooltip: {
        //             trigger: 'item'
        //         },
        //         legend: {
        //             top: '5%',
        //             left: 'center'
        //         },
        //         series: [
        //             {
        //                 name: '播放量',
        //                 type: 'pie',
        //                 radius: ['40%', '70%'],
        //                 label : {
        //                     show: false,
        //                     position: 'center'
        //                 },
        //                 data: this.topVideos.map(video => ({ value: video.play, name: `${video.title}` })),
        //                 emphasis: {
        //                     label: {
        //                         show: true,
        //                         fontSize: '20',
        //                     }
        //                 },
        //                 labelLine: {
        //                     show: false
        //                 },
        //                     itemStyle: {
        //                     borderRadius: 10,
        //                     borderColor: '#fff',
        //                     borderWidth: 2
        //                 },
        //             }
        //         ]
        //     };
        //     myChart.setOption(option);
        // },

        initDailyUpload() {
            const chartDom = document.getElementById('dailyUpload');
            const myChart = echarts.init(chartDom);
            const option = {
                title: {
                    text: '近7日投稿量'
                },
                tooltip: {
                    trigger: 'axis'
                },
                xAxis: {
                    type: 'category',
                    data: this.DailyUploadxAxisData,
                    axisLabel: {
                        interval: 0,
                        rotate: 45,
                    }
                },
                yAxis: {
                    type: 'value'
                },
                series: [
                    {
                        name: '投稿数',
                        data: this.DailyUploadseriesData,
                        type: 'bar',
                        color: '#00aeec',
                        smooth: true,
                    }
                ]
            };

            myChart.setOption(option);
        }
    },
    mounted() {
        this.fetchData();
    }
}
</script> -->

<style scoped>
.eltext {
    font-size: 50px;
    font-weight: 100;
}

.col {
    border-radius: 10px;
    margin: 10px;
    padding: 10px;
    background-color: white;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    font-size: 16px;
}

.content {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    height: 200px;
}

.main {
    padding: 40px;
    margin-top: 16px;
}

.pie {
    display: flex;
    width: 100%;
    justify-content: center;
    align-items: center;
}
</style>