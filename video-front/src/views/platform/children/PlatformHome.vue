<template>
    <div class="platform-home">
        <NavBar :navBarItem="navBarData" @clickBarItem="clickBarItem" :style="isNavBarShow ? '' : 'display: none;'"></NavBar>
        <div class="data-warp">
            <el-row class="row" style="margin-bottom: 16px;">
                <el-col :span="6" style="padding-left: 8px; padding-right: 8px">
                    <el-card shadow="never" style="height: 100px; border-radius: 16px; background-color: rgb(245,252,254); border: 0">
                        <div class="title">
                             <Icon icon ="ri:user-heart-line" width="18" height="18" />
                             <p class="text">粉丝数</p>
                        </div>
                        <p style="font-size: 22px; font-weight: 800; color: rgb(255, 102, 153)">{{ formatNumber(this.userInfo.fansCount) }}</p>
                    </el-card>
                </el-col >
                <el-col :span="6" style="padding-left: 8px; padding-right: 8px">
                    <el-card shadow="never" style="height: 100px; border-radius: 16px; background-color: rgb(245,252,254); border: 0">
                        <div class="title">
                            <Icon icon="ph:play-duotone" width="18" height="18" />
                            <p class="text">播放量</p>
                        </div>
                        <p style="font-size: 22px; font-weight: 800; color: rgb(255, 102, 153)">{{ formatNumber(this.userInfo.playCount) }}</p>
                    </el-card>
                </el-col>
                <el-col :span="6" style="padding-left: 8px; padding-right: 8px">
                    <el-card shadow="never" style="height: 100px; border-radius: 16px; background-color: rgb(245,252,254); border: 0">
                        <div class="title">
                            <Icon icon="uim:comment" width="18" height="18" />
                            <p class="text">评论</p>
                        </div>
                        <p style="font-size: 22px; font-weight: 800; color: rgb(255, 102, 153)">{{ formatNumber(this.userInfo.commentCount) }}</p>
                    </el-card>
                </el-col>
                <el-col :span="6" style="padding-left: 8px; padding-right: 8px">
                    <el-card shadow="never" style="height: 100px; border-radius: 16px; background-color: rgb(245,252,254); border: 0">
                        <div class="title">
                            <Icon icon="mingcute:danmaku-line" width="18" height="18" />
                            <p class="text">弹幕</p>
                        </div>
                        <p style="font-size: 22px; font-weight: 800; color: rgb(255, 102, 153)">{{ formatNumber(this.userInfo.danmuCount) }}</p>
                    </el-card>
                </el-col>
            </el-row>
            <el-row class="row">
                <el-col :span="6" style="padding-left: 8px; padding-right: 8px">
                    <el-card shadow="never" style="height: 100px; border-radius: 16px; background-color: rgb(245,252,254); border: 0">
                        <div class="title">
                            <Icon icon="iconamoon:like-duotone" width="18" height="18" />
                            <p class="text">点赞</p>
                        </div>
                        <p style="font-size: 22px; font-weight: 800; color: rgb(255, 102, 153)">{{ formatNumber(this.userInfo.loveCount) }}</p>
                    </el-card>
                </el-col>
                <el-col :span="6" style="padding-left: 8px; padding-right: 8px">
                    <el-card shadow="never" style="height: 100px; border-radius: 16px; background-color: rgb(245,252,254); border: 0">
                        <div class="title">
                            <Icon icon="icon-park-twotone:share-two" width="18" height="18" />
                            <p class="text">分享</p>
                        </div>
                        <p style="font-size: 22px; font-weight: 800; color: rgb(255, 102, 153)">{{ formatNumber(this.userInfo.shareCount) }}</p>
                    </el-card>
                </el-col>
                <el-col :span="6" style="padding-left: 8px; padding-right: 8px">
                    <el-card shadow="never" style="height: 100px; border-radius: 16px; background-color: rgb(245,252,254); border: 0">
                        <div class="title">
                            <Icon icon="lets-icons:star-duotone" width="18" height="18" />
                            <p class="text">收藏</p>
                        </div>
                        <p style="font-size: 22px; font-weight: 800; color: rgb(255, 102, 153)">{{ formatNumber(this.userInfo.collectCount) }}</p>
                    </el-card>
                </el-col>
                <el-col :span="6" style="padding-left: 8px; padding-right: 8px">
                    <el-card shadow="never" style="height: 100px; border-radius: 16px; background-color: rgb(245,252,254); border: 0">
                        <div class="title">
                            <i class="iconfont icon-toubi" style="width: 18px; height: 18px" ></i>
                            <p class="text">投币</p>
                        </div>
                        <p style="font-size: 22px; font-weight: 800; color: rgb(255, 102, 153)">{{ formatNumber(this.userInfo.coinCount) }}</p>
                    </el-card>
                </el-col>
            </el-row>
        </div>
        <div class="data-show">
            <el-card shadow="never" style="margin-top: 32px; padding: 24px; width: 90%; border-radius: 12px">
                <div id="line" style="width: 100%; height: 400px"></div>
            </el-card>
        </div>
    </div>
</template>

<script>
import NavBar from '@/components/navbar/NavBar.vue';
import { Icon } from '@iconify/vue';
import * as echarts from 'echarts';

const option = {
    title: {
        text: '近7日播放量'
    },
    tooltip: {
        trigger: 'axis'
    },
    xAxis: {
        type: 'category',
        data: ['2024-12-26', '2024-12-27', '2024-12-28', '2024-12-29', '2024-12-30', '2024-12-31', '2025-01-01', '2025-01-02']
    },
    yAxis: {
        type: 'value'
    },
    series: [
        {
            name: '播放量',
            data: [1353, 1681, 4236, 5439, 5183, 2368, 1279, 3451],
            type: 'line',
            color: '#00aeec',
            smooth: true,
        }
    ]
};

export default {
    name: "PlatformHome",
    components: {
        NavBar,
        Icon,
    },
    data() {
        return {
            isNavBarShow: true,
            navBarData: [
                { name: "视频数据"},
            ],
            userInfo: [],
        }
    },
    methods: {
        formatNumber(num) {
            if (num == null || isNaN(num)) {
                num = 0;
            }

            return num.toString().replace(/\d+/, function (n) { // 先提取整数部分
                return n.replace(/(\d)(?=(\d{3})+$)/g, function ($1) {
                    return $1 + ",";
                });
            });
        },

        async getUserInfo() {
            const res = await this.$get(`/user/info/get-one?uid=${this.$store.state.user.uid}`);
            if (res.data.code === 200) {
                this.userInfo = res.data.data;
                console.log("userInfo: ", this.userInfo);
            }
        },
    },
    async mounted() {
        await this.getUserInfo();
        let chartDom = document.getElementById('line');
        let myChart = echarts.init(chartDom);

        await this.$get(`/video/daily-play-count?uid=${this.$store.state.user.uid}`, {
            headers: { Authorization: "Bearer " + localStorage.getItem("token") }
        }).then(res => {
            if (res.data.code === 200) {
                // option.xAxis.data = res.data.data.map(item => item.date);
                // option.series[0].data = res.data.data.map(item => item.total_play_count);
                // console.log("option: ", option);
                myChart.setOption(option);
            }
        });
    }
}
</script>

<style scoped>
.data-warp {
    width: 100%;
    height: 100%;
    display: block;
    margin-top: 24px;
}

.row {
    padding-left: 14px;
    padding-right: 14px;
}

.title {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
}

.text {
    font-size: 14px;
    font-weight: 400;
    color: rgb(97, 102, 109);
    margin-left: 4px
}

.data-show {
    height: 100%;
    width: 100%;
    display: flex;
    justify-content: center;
    margin-top: 24px;
}
</style>