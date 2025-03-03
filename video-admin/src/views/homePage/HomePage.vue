<!-- <template>
    <div class="flex-fill" style="background-color: rgb(250,250,250);">
        <div class="top">
            <div class="avatar">
                <el-avatar :size="100" :src="this.userInfo.avatar_url" />
            </div>
            <div class="info">
                <p>{{ greeting }}，{{this.userInfo.nickname}}，{{ this.hitokoto }} </p>
                <p>{{this.userInfo.bio}}</p>
            </div>
        </div>
        <el-row>
            <el-col :span=12>
                <el-card class="col">
                    <template #header>
                        <div style="margin: 10px">最新视频</div>
                    </template>
                    <el-row>
                        <el-col :span=24>
                            <el-carousel indicator-position="outside" :interval="4000" class="carousel" motion-blur type="card" height="250px" >
                                <el-carousel-item v-for="item in videoInfo" :key="item.id" class="carousel-item">
                                    <img :src="item.coverUrl" style="width: 1000px; height: 280px; border-radius: 15px" />
                                    <div class="bottom">
                                        <span>{{ item.title }}</span>
                                    </div>
                                </el-carousel-item>
                            </el-carousel>
                        </el-col>
                    </el-row>
                </el-card>
            </el-col>
            <el-col :span=12>
                <el-card class="col">
                    <template #header>
                        <div style="margin: 10px">待审核稿件数</div>
                    </template>
                    <div style="height: 270px;">
                        <el-row>
                            <el-col :span=8 class="status">
                                <el-text class="eltext" type="warning"> {{  this.v_waitting }} </el-text>
                                <el-text type="warning"> 待审核稿件数 </el-text>
                            </el-col>
                            <el-col :span=8 class="status">
                                <el-text class="eltext" type="success"> {{  this.v_pass }} </el-text>
                                <el-text type="success"> 通过稿件数 </el-text>
                            </el-col>
                            <el-col :span=8 class="status">
                                <el-text class="eltext" type="danger"> {{  this.v_fail }} </el-text>
                                <el-text type="danger"> 未通过稿件数 </el-text>
                            </el-col>
                        </el-row>
                    </div>
                </el-card>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span=24>
                <div class="data-show">
                    <el-card shadow="never" style="margin: 16px; padding: 24px; width: 100%; border-radius: 12px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1)">
                        <div id="line" style="width: 100%; height: 400px"></div>
                    </el-card>
                </div>
            </el-col>
        </el-row>
    </div>
</template> -->

<!-- <script>

import * as echarts from 'echarts';

const option = {
    title: {
        text: '近7日投稿量'
    },
    tooltip: {
        trigger: 'axis'
    },
    xAxis: {
        type: 'category',
        data: []
    },
    yAxis: {
        type: 'value'
    },
    series: [
        {
            name: '投稿数',
            data: [],
            type: 'bar',
            color: '#00aeec',
            smooth: true,
        }
    ]
};

export default {
    name: "HomePage",
    data() {
        return {
            userInfo:[],
            greeting: '',
            hitokoto: '',
            videoInfo: [],
            v_waitting: 0,
            v_pass: 0,
            v_fail: 0
            
        };
    },
    methods: {
        getUserInfo() {
            this.userInfo = this.$store.state.user;
        },

        setGreeting() {
            const hour = new Date().getHours();
            if (hour < 12) {
                this.greeting = '早上好';
            } else if (hour < 18) {
                this.greeting = '下午好';
            } else {
                this.greeting = '晚上好';
            }
        },

        async fetchHitokoto() {
            const res = await fetch('https://international.v1.hitokoto.cn/');
            const data = await res.json();
            this.hitokoto = data.hitokoto;
        },

        async fetchVideoByDate() {
            const res = await this.$get("/video/latest")
            this.videoInfo = res.data.data;
            console.log(this.videoInfo);
        },

        async getVStatus() {
            const waitting = await this.$get('/review/video/total', {
                params: {
                    vstatus: 0,
                },
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token"),
                },
            });

            this.v_waitting = waitting.data.data;

            const pass = await this.$get('/review/video/total', {
                params: {
                    vstatus: 1,
                },
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token"),
                },
            });

            this.v_pass = pass.data.data

            const fail = await this.$get('/review/video/total', {
                params: {
                    vstatus: 2,
                },
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token"),
                },
            });

            this.v_fail = fail.data.data
        },

    },
    mounted() {
        this.getUserInfo();
        this.setGreeting();
        this.fetchHitokoto();
        this.fetchVideoByDate();
        this.getVStatus();
        
        let chartDom = document.getElementById('line');
        let myChart = echarts.init(chartDom);

        this.$get("/video/uploaded-last-7-days", {
            headers: { Authorization: "Bearer " + localStorage.getItem("token"), }
        }).then(res => {
            if (res.data.code === 200) {
                option.xAxis.data = res.data.data.map(item => item.date);
                option.series[0].data = res.data.data.map(item => item.total_uploads);
                myChart.setOption(option);
            }
        })
    }
}
</script> -->

<script setup>
import { ref, onMounted, computed} from 'vue';
import { useStore } from 'vuex';
import { VueUiQuickChart } from 'vue-data-ui';
import { get } from '@/network/request';

const store = useStore();

const userInfo = ref([]);
const greeting = ref('');
const hitokoto = ref('');
const videoInfo = ref([]);
const v_waitting = ref(0);
const v_pass = ref(0);
const v_fail = ref(0);
const dailyUpload = ref([]);

const DailyUploadxAxisData = computed(() => dailyUpload.value.map(item => item.date));
const dailyUploadConfig = ref({ "responsive": false, "backgroundColor": "#FFFFFF", "barAnimated": true, "barGap": 12, "barStrokeWidth": 1, "blurOnHover": true, "chartIsBarUnderDatasetLength": 6, "color": "#2D353C", "dataLabelFontSize": 14, "dataLabelRoundingPercentage": 1, "dataLabelRoundingValue": 1, "donutHideLabelUnderPercentage": 3, "donutLabelMarkerStrokeWidth": 1, "donutRadiusRatio": 0.4, "donutShowTotal": true, "donutStrokeWidth": 2, "donutThicknessRatio": 0.18, "donutTotalLabelFontSize": 24, "donutTotalLabelOffsetY": 0, "donutTotalLabelText": "Total", "donutUseShadow": false, "donutShadowColor": "#1A1A1A", "fontFamily": "inherit", "height": 338, "legendFontSize": 12, "legendIcon": "circleFill", "legendIconSize": 12, "lineAnimated": true, "lineSmooth": true, "lineStrokeWidth": 2, "paletteStartIndex": 0, "showDataLabels": true, "showLegend": true, "showTooltip": true, "showUserOptions": true, "userOptionsButtons": { "tooltip": true, "pdf": true, "img": true, "fullscreen": true, "annotator": true }, "userOptionsButtonTitles": { "open": "Open options", "close": "Close options", "tooltip": "Toggle tooltip", "pdf": "Download PDF", "img": "Download PNG", "fullscreen": "Toggle fullscreen", "annotator": "Toggle annotator" }, "title": "近7日投稿数量", "titleBold": true, "titleFontSize": 16, "titleTextAlign": "center", "tooltipCustomFormat": null, "tooltipBorderRadius": 4, "tooltipBorderColor": "#e1e5e8", "tooltipBorderWidth": 1, "tooltipFontSize": 14, "tooltipBackgroundOpacity": 30, "tooltipPosition": "center", "tooltipOffsetY": 24, "useCustomLegend": false, "valuePrefix": "", "valueSuffix": "", "width": 600, "xyAxisStroke": "#CCCCCC", "xyAxisStrokeWidth": 1, "xyGridStroke": "#e1e5e8", "xyGridStrokeWidth": 0.5, "xyHighlighterColor": "#000000", "xyHighlighterOpacity": 0.05, "xyLabelsXFontSize": 8, "xyLabelsYFontSize": 12, "xyPaddingBottom": 48, "xyPaddingLeft": 48, "xyPaddingRight": 12, "xyPaddingTop": 24, "xyPeriods": DailyUploadxAxisData, "xyPeriodLabelsRotation": 0, "xyScaleSegments": 10, "xyShowAxis": true, "xyShowGrid": true, "xyShowScale": true, "yAxisLabel": "投稿数量", "xAxisLabel": "日期", "axisLabelsFontSize": 12, "userOptionsPosition": "right", "zoomXy": true, "zoomColor": "#CCCCCC", "zoomHighlightColor": "#4A4A4A", "zoomFontSize": 14, "zoomUseResetSlot": false, "zoomMinimap": { "show": true, "smooth": true, "selectedColor": "#1f77b4", "selectedColorOpacity": 0.2, "lineColor": "#1f77b4", "selectionRadius": 2, "indicatorColor": "#1A1A1A" }, "zoomStartIndex": null, "zoomEndIndex": null, "showUserOptionsOnChartHover": false, "keepUserOptionsStateOnChartLeave": true });
const dailyUploadDataFake = ref([25, 40, 55, 31, 60, 78, 90]);

const getUserInfo = () => {
    userInfo.value = store.state.user;
};

const setGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) {
        greeting.value = '早上好';
    } else if (hour < 18) {
        greeting.value = '下午好';
    } else {
        greeting.value = '晚上好';
    }
};

const fetchHitokoto = async () => {
    const res = await fetch('https://international.v1.hitokoto.cn/');
    const data = await res.json();
    hitokoto.value = data.hitokoto;
};

const fetchVideoByDate = async () => {
    const res = await get("/video/latest");
    videoInfo.value = res.data.data;
    console.log(videoInfo.value);
};

// const getDailyUploadData = async () => {
//     const res = await get("/video/uploaded-last-7-days", {
//         headers: { Authorization: "Bearer " + localStorage.getItem("token"), }
//     });

//     if (res.status === 200) {
//         dailyUpload.value = res.data.data;
//     }
// }

const getVStatus = async () => {
    const waitting = await get('/review/video/total', {
        params: {
            vstatus: 0,
        },
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token"),
        },
    });

    v_waitting.value = waitting.data.data;

    const pass = await get('/review/video/total', {
        params: {
            vstatus: 1,
        },
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token"),
        },
    });

    v_pass.value = pass.data.data;

    const fail = await get('/review/video/total', {
        params: {
            vstatus: 2,
        },
        headers: {
            Authorization: "Bearer " + localStorage.getItem("token"),
        },
    });

    v_fail.value = fail.data.data;
};

onMounted(() => {
    getUserInfo();
    setGreeting();
    fetchHitokoto();
    fetchVideoByDate();
    getVStatus();


});
</script>

<template>
    <div class="flex-fill" style="background-color: rgb(250,250,250);">
        <div class="top">
            <div class="avatar">
                <el-avatar :size="100" :src="userInfo.avatar_url" />
            </div>
            <div class="info">
                <p>{{ greeting }}，{{ userInfo.nickname }}，{{ hitokoto }} </p>
                <p>{{ userInfo.bio }}</p>
            </div>
        </div>
        <el-row>
            <el-col :span="12">
                <el-card class="col">
                    <template #header>
                        <div style="margin: 10px">最新视频</div>
                    </template>
                    <el-row>
                        <el-col :span="24">
                            <el-carousel indicator-position="outside" :interval="4000" class="carousel" motion-blur type="card" height="250px">
                                <el-carousel-item v-for="item in videoInfo" :key="item.id" class="carousel-item">
                                    <img :src="item.coverUrl" style="width: 1000px; height: 280px; border-radius: 15px" />
                                    <div class="bottom">
                                        <span>{{ item.title }}</span>
                                    </div>
                                </el-carousel-item>
                            </el-carousel>
                        </el-col>
                    </el-row>
                </el-card>
            </el-col>
            <el-col :span="12">
                <el-card class="col">
                    <template #header>
                        <div style="margin: 10px">待审核稿件数</div>
                    </template>
                    <div style="height: 270px;">
                        <el-row>
                            <el-col :span="8" class="status">
                                <el-text class="eltext" type="warning"> {{ v_waitting }} </el-text>
                                <el-text type="warning"> 待审核稿件数 </el-text>
                            </el-col>
                            <el-col :span="8" class="status">
                                <el-text class="eltext" type="success"> {{ v_pass }} </el-text>
                                <el-text type="success"> 通过稿件数 </el-text>
                            </el-col>
                            <el-col :span="8" class="status">
                                <el-text class="eltext" type="danger"> {{ v_fail }} </el-text>
                                <el-text type="danger"> 未通过稿件数 </el-text>
                            </el-col>
                        </el-row>
                    </div>
                </el-card>
            </el-col>
        </el-row>
        <el-row>
            <el-col :span="24">
                <div class="data-show">
                    <el-card shadow="never" style="margin: 16px; padding: 24px; width: 100%; border-radius: 12px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1)">
                        <VueUiQuickChart :dataset="dailyUploadDataFake" :config="dailyUploadConfig" />
                    </el-card>
                </div>
            </el-col>
        </el-row>
    </div>
</template>

<style scoped>
.top {
    height: 200px;
    border-radius: 10px;
    margin: 10px;
    padding: 10px;
    background-color: white;
    display: flex;
    justify-content: flex-start;
    align-content: center;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.avatar {
    display: flex;
    justify-content: center;
    align-content: center;
    margin-left: 20px;
    padding-top: 39px;
    padding-bottom: 39px;
}

.info {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-content: center;
    margin-left: 40px;
    font-size: 18px;
    font-weight: 400;
}

.col {
    border-radius: 10px;
    margin: 10px;
    padding: 10px;
    background-color: white;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    font-size: 16px;
}

.carousel-item {
    margin: 10px;
    display: flex;
    justify-content: center;
    flex-direction: column;
}

.carousel {
    justify-content: center;
}

.eltext {
    font-size: 150px;
    font-weight: 100;
}

.status {
    display: flex;
    justify-content: center;
    align-content: center;
    flex-direction: column;
    padding: 10px;
}

.data-show {
    height: 100%;
    width: 100%;
    display: flex;
    justify-content: center;
    margin-top: 24px;
}
</style>