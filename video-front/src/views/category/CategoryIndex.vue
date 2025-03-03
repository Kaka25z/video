<template>
    <div class="index">
        <div class="large-header">
            <HeaderBar :isFixHeaderBar="isFixHeaderBar"></HeaderBar>
            <header>
                <div class="view">
                    <img src="~assets/img/bilibili/bilibili-winter-view-1.jpg" class="morning" alt="">
                    <img src="~assets/img/bilibili/bilibili-winter-view-2.jpg" class="afternoon" alt="">
                    <video autoplay loop muted class="evening">
                        <source src="~assets/img/bilibili/bilibili-winter-view-3.webm" type="video/webm" />
                    </video>
                    <img src="~assets/img/bilibili/bilibili-winter-view-3-snow.png" class="window-cover" alt="">
                </div>
                <div class="tree">
                    <img src="~assets/img/bilibili/bilibili-winter-tree-1.png" class="morning" alt="">
                    <img src="~assets/img/bilibili/bilibili-winter-tree-2.png" class="afternoon" alt="">
                    <img src="~assets/img/bilibili/bilibili-winter-tree-3.png" class="evening" alt="">
                </div>
                <div class="taper-line"></div>
            </header>
            <HeaderChannel></HeaderChannel>
        </div>

        <div class="main__layout">
            <div class="flex-wapper">
                <div class="channel-nav">
                    <div class="channel-nav-name">
                        {{ channelName }}
                    </div>
                </div>
            </div>
            <div class="video-grid" v-for="scList in currentSubChannels" :key="scList.scId">
                <div class="aera-header">
                    <div class="left">
                        <span>{{ scList.scName }}</span>
                    </div>
                    <div class="right">
                        <div class="feed-roll-btn">
                            <div class="roll-btn" @click="RefreshVideo(scList.scId); refreshTime++;">
                                <i class="iconfont icon-shuaxin" :style="`transform: rotate(${refreshTime * 360}deg);`"></i>
                                <span>换一换</span>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="video-card-list">
                    <template v-if="randomVideos[scList.scId] && randomVideos[scList.scId].length">
                        <div class="feed-card" v-for="(video, index) in randomVideos[scList.scId]" :key="index">
                            <div class="video-card">
                                <div class="video-card__skeleton" :class="loadingRandom ? 'loading_animation' : 'hide'">
                                    <div class="video-card__skeleton--cover"></div>
                                    <div class="video-card__skeleton--info">
                                        <div class="video-card__skeleton--right">
                                            <p class="video-card__skeleton--text"></p>
                                            <p class="video-card__skeleton--text short"></p>
                                            <p class="video-card__skeleton--light"></p>
                                        </div>
                                    </div>
                                </div>
                                <div class="video-card__wrap" v-if="!loadingRandom">
                                    <a :href="`/video/${video.video.vid}`" target="_blank">
                                        <div class="video-card__image">
                                            <div class="video-card__image--wrap">
                                                <picture class="video-card__cover">
                                                    <img :src="video.video.coverUrl" :alt="video.video.title">
                                                </picture>
                                            </div>
                                            <div class="video-card__mask">
                                                <div class="video-card__stats">
                                                    <div class="video-card__stats--left">
                                                        <span class="video-card__stats--item">
                                                            <i class="iconfont icon-bofangshu"></i>
                                                            <span class="video-card__stats--text">
                                                                {{ handleNum(video.stats.play) }}
                                                            </span>
                                                        </span>
                                                        <span class="video-card__stats--item">
                                                            <i class="iconfont icon-danmushu"></i>
                                                            <span class="video-card__stats--text">
                                                                {{ handleNum(video.stats.danmu) }}
                                                            </span>
                                                        </span>
                                                    </div>
                                                    <div class="video-card__stats__duration">
                                                        {{ handleDuration(video.video.duration) }}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </a>
                                    <div class="video-card__info">
                                        <div class="video-card__info--right">
                                            <h3 class="video-card__info--tit">
                                                <a :href="`/video/${video.video.vid}`" target="_blank">
                                                    {{ video.video.title }}
                                                </a>
                                            </h3>
                                            <div class="video-card__info--bottom">
                                                <div class="video-card__info--icon-text" :style="'display: none;'">已关注</div>
                                                <a class="video-card__info--owner"
                                                    :href="`/space/${video.user.uid}`" target="_blank">
                                                    <i class="iconfont icon-uper" :style="''"></i>
                                                    <span class="video-card__info--author">{{ video.user.nickname }}</span>
                                                    <span class="video-card__info--date">
                                                        · {{ handleDate(video.video.uploadDate) }}
                                                    </span>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                    <el-empty v-else class="empty-center" description="暂无数据"></el-empty>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import HeaderBar from '@/components/headerBar/HeaderBar.vue';
import HeaderChannel from '@/components/headerChannel/HeaderChannel.vue';
import { handleTime, handleNum, handleDate } from '@/utils/utils.js';
// let headerHight, bottomDistance;

export default {
    name: "CategoryIndex",
    components: {
        HeaderBar,
        HeaderChannel,
    },
    data() {
        return {
            // 是否是固钉导航栏
            isFixHeaderBar: false,
            // 是否是固钉频道栏
            isFixChannel: false,
            // 是否显示更多频道
            isChannelDown: false,
            // 随机推荐视频列表
            randomVideos: {},
            // 累加视频列表
            cumulativeVideos: [],
            // 累加视频id列表
            vids: [],
            // 是否正在加载随机推荐
            loadingRandom: true,
            // 是否还有更多累加视频
            hasMore: true,
            // 是否正在加载更多视频中
            loadingMore: false,
            // 刷新次数
            refreshTime: 0,

            currentChannel: [],

            currentSubChannelsList: [],
        }
    },
    computed: {
        // 频道列表
        channels() {
            return this.$store.state.channels;
        },
        channelName() {
            const mcId = this.$route.params.mcId;
            const channel = this.channels.find(channel => channel.mcId === mcId);
            return channel ? channel.mcName : '频道未找到';
        },
        currentSubChannels() {
            const mcId = this.$route.params.mcId;
            const channel = this.channels.find(channel => channel.mcId === mcId);
            return channel ? channel.scList.slice().reverse() : [];
        }
    },
    methods: {

        async getRandomVideos(scId) {

            this.loadingRandom = true;

            console.log("方法调用")

            const res = await this.$get("/video/get-by-main-category", {
                params: {
                    mcId: this.$route.params.mcId,
                    scId: scId,
                }
            });
            if (res.data.data) {
                this.randomVideos[scId] = res.data.data;
                this.loadingRandom = false;
            }
        },

        async RefreshVideo(scId) {
            this.getRandomVideos(scId);
        },

        initHeader() {
            let startingPoint;
            const header = document.querySelector('header');
            let isMoving = false;

            this.handleMouseMove = (e) => {
                const headerRect = header.getBoundingClientRect(); // 动态获取header的位置
                if (
                    e.clientX >= headerRect.left && e.clientX <= headerRect.right &&
                    e.clientY >= headerRect.top && e.clientY <= headerRect.bottom
                ) {
                    // 当鼠标进入头图范围就开始动态效果
                    if (!isMoving) {
                        startingPoint = e.clientX;
                        header.classList.add('moving');
                        isMoving = true;
                    }
                    let percentage = (e.clientX - startingPoint) / window.outerWidth + 0.5;
                    header.style.setProperty('--percentage', percentage);
                } else {
                    // 鼠标移出头图范围，效果复原
                    if (isMoving) {
                        header.classList.remove('moving');
                        header.style.setProperty('--percentage', 0.5);
                        isMoving = false;
                    }
                }
            }

            this.handleMouseOut = (e) => {
                if (e.relatedTarget === null) {
                    // 鼠标离开窗口时，头图回到原位
                    header.classList.remove('moving');
                    header.style.setProperty('--percentage', 0.5);
                    isMoving = false;
                }
            }

            // 监听窗口鼠标移动事件，触发头图转变效果
            document.addEventListener('mousemove', this.handleMouseMove);
            // 检测鼠标是否离开窗口
            document.addEventListener('mouseout', this.handleMouseOut);
        },

        // 处理播放时长
        handleDuration(time) {
            return handleTime(time);
        },

        // 处理大于一万的数字
        handleNum(number) {
            return handleNum(number);
        },

        // 处理投稿时间
        handleDate(date) {
            return handleDate(date);
        },
    },
    async mounted() {
        // 初始化头图的监听器
        this.initHeader();
        // 窗口滚动时根据高度判断是否显示固钉导航栏和固钉频道栏
        this.el = document.documentElement;

        this.currentSubChannelsList = this.currentSubChannels;

        if (this.currentSubChannels.length > 0) {
            for (const scList of this.currentSubChannels) {
                await this.getRandomVideos(scList.scId);
            }
        } else {
            this.$watch('currentSubChannels', async (newVal) => {
                if (newVal.length > 0) {
                    for (const scList of newVal) {
                        await this.getRandomVideos(scList.scId);
                    }
                }
            });
        }


    },
    beforeUnmount() {
        document.removeEventListener('mousemove', this.handleMouseMove);
        document.removeEventListener('mouseout', this.handleMouseOut);
        window.removeEventListener('scroll', this.handleScroll);
    }
}
</script>

<style scoped>
@media (min-width: 1367px) and (max-width: 1700.9px) {
    .header-banner__inner {
        padding: 0 64px;
    }
}

.index {
    position: relative;
    margin: 0 auto;
    max-width: 2560px;
    background-color: var(--bg1);
}

.large-header {
    background-color: #fff;
    min-height: 64px;
    position: relative;
    margin: 0 auto;
    max-width: 2560px;
    width: 100%;
    color: #000;
}

.header__banner {
    position: relative;
    z-index: 0;
    display: flex;
    -ms-flex-pack: center;
    justify-content: center;
    margin: 0 auto;
    min-width: 1000px;
    min-height: 155px;
    height: 9.375vw;
    max-height: 240px;
    background-color: #e3e5e7;
    background-position: center 0;
    background-size: cover;
    background-repeat: no-repeat;
}

.v-img {
    display: inline-block;
    line-height: 1;
    width: 100%;
    height: 100%;
    vertical-align: middle;
    background-color: var(--graph_bg_regular);
}

.v-img img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: inherit;
}

.banner-img {
    position: absolute;
    object-fit: cover;
}

.banner-img img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: inherit;
}

.header-banner__inner {
    position: relative;
    width: 100%;
    max-width: 2078px;
    margin: 0 auto;
    display: flex;
    align-items: flex-end;
}

@media (max-width: 1366.9px) {
    .header-banner__inner {
        padding: 0 56px;
    }
}

@media (min-width: 1367px) and (max-width: 1700.9px) {
    .header-banner__inner {
        padding: 0 64px;
    }
}

@media (min-width: 1701px) and (max-width: 2199.9px) {
    .header-banner__inner {
        padding: 0 96px;
        max-width: 2270px;
    }
}

@media (min-width: 2200px) {
    .header-banner__inner {
        max-width: 2078px;
    }
}

.logo-box {
    z-index: 1;
    display: inline-block;
    width: 150px;
    height: 50%;
}

.header-banner__inner img {
    cursor: pointer;
    position: relative;
    top: 5;
    right: 0;
    margin-bottom: 20px;
    margin-left: 15px;
    width: 150px;
    height: 50px;
    filter: drop-shadow(3px 5px 3px rgba(0, 0, 0, 0.5));
}

header {
    position: relative;
    z-index: 0;
    display: flex;
    -ms-flex-pack: center;
    justify-content: center;
    margin: 0 auto;
    min-width: 1000px;
    min-height: 155px;
    height: 9.375vw;
    max-height: 240px;
    background-color: #e3e5e7;
    background-position: center 0;
    background-size: cover;
    background-repeat: no-repeat;
    overflow: hidden;
    --percentage: 0.5;
}

header .view,
header .tree {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    display: flex;
    justify-content: center;
    align-items: center;
}

header .view img,
header .view video,
header .tree img {
    position: absolute;
    display: block;
    width: 120%;
    height: 100%;
    object-fit: cover;
}

header .morning {
    z-index: 20;
    opacity: calc(1 - (var(--percentage) - 0.25) / 0.25);
}

header .afternoon {
    z-index: 10;
    opacity: calc(1 - (var(--percentage) - 0.5) / 0.5);
}

header .view {
    transform: translatex(calc(var(--percentage) * 100px));
}

header .tree {
    transform: translatex(calc(var(--percentage) * 50px));
    filter: blur(3px);
}

header .view,
header .tree,
header .morning,
header .afternoon {
    transition: .2s all ease-in;
}

header.moving .view,
header.moving .tree,
header.moving .morning,
header.moving .afternoon {
    transition: none;
}

header .window-cover {
    opacity: calc((var(--percentage) - 0.9) / 0.1);
}

.taper-line {
    position: absolute;
    top: 0;
    left: 0;
    z-index: 0;
    width: 100%;
    height: 100px;
    background: linear-gradient(rgba(0, 0, 0, .4), transparent);
    pointer-events: none;
}

.header-channel-fixed {
    width: 100%;
    min-width: 1100px;
    max-width: 2560px;
    display: flex;
    justify-content: center;
    background: var(--bg1_float);
    z-index: 1001;
    position: fixed;
    top: 63px;
    letter-spacing: 2px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, .08);
    animation: headerSlideDown .2s linear forwards;
}

@keyframes headerSlideDown {
    0% {
        opacity: 0;
    }

    100% {
        opacity: 1;
    }
}

.header-channel-fixed {
    --left_width: 140px;
    --left_width_item: 70px;
    --item_height: 28px;
}

.header-channel-fixed-container {
    max-height: 56px;
    overflow: hidden;
    transition: max-height .2s;
    display: flex;
    align-items: center;
    position: relative;
    width: 100%;
    padding: 14px var(--layout-padding, 60px) 14px var(--layout-padding, 30px);
}

.header-channel-fixed-down {
    max-height: 150px;
}

.header-channel-fixed-left {
    display: flex;
    min-width: var(--left_width);
    width: var(--left_width);
    flex-wrap: wrap;
    color: var(--text2);
    font-size: 14px;
    align-self: start;
}

.left-fixed-channel {
    cursor: pointer;
    display: flex;
    width: var(--left_width_item);
    height: 28px;
    margin-bottom: 14px;
    align-items: center;
}

.left-fixed-channel .iconfont {
    color: var(--text1);
    font-size: 21px;
    display: inline-block;
    position: relative;
    margin-right: 5px;
}

.left-fixed-channel:hover,
.left-fixed-channel:hover .iconfont {
    color: var(--brand_pink);
}

.header-channel-fixed-center {
    height: 14px;
    width: 1px;
    margin-right: 10px;
    background: var(--line_regular);
}

.header-channel-fixed-right {
    display: inline-grid;
    flex: 1;
    height: 100%;
    position: relative;
}

.header-channel-fixed-right-item {
    height: var(--item_height);
    line-height: var(--item_height);
    border: 1px solid var(--line_light);
    background: var(--graph_bg_thin);
    border-radius: 6px;
    text-align: center;
    cursor: pointer;
    color: var(--text2);
}

.header-channel-fixed-right-item:hover {
    background: var(--graph_bg_thick);
    transition: background 0.2s;
}

.left-top .header-channel-fixed-right-item:nth-of-type(23),
.left-top .header-channel-fixed-right-item:nth-of-type(24),
.left-top .header-channel-fixed-right-item:nth-of-type(26),
.left-bottom .header-channel-fixed-right-item:nth-of-type(23),
.left-bottom .header-channel-fixed-right-item:nth-of-type(24),
.left-bottom .header-channel-fixed-right-item:nth-of-type(26) {
    letter-spacing: 0px;
}

.header-channel-fixed-right-left {
    display: inline-grid;
    position: relative;
}

.header-channel-fixed-right-left .left-top {
    display: inline-grid;
    grid-template-rows: repeat(2, 1fr);
    position: relative;
    grid-auto-flow: column;
}

.header-channel-fixed-right-right {
    display: inline-grid;
    grid-template-columns: repeat(3, 1fr);
    grid-auto-flow: column;
    grid-template-rows: var(--item_height) var(--item_height);
    grid-column: span 3;
}

.header-channel-fixed-right .left-bottom {
    display: inline-grid;
    grid-template-rows: repeat(1, 1fr);
    position: relative;
}

.header-channel-fixed-right .left-bottom,
.header-channel-fixed-right-right,
.header-channel-fixed-right-left .left-top,
.header-channel-fixed-right {
    grid-gap: 14px 10px;
}

@media (max-width: 1366.9px) {
    .header-channel-fix {
        --left_width: 128px;
        --left_width_item: 64px;
    }

    .header-channel-fixed-right {
        grid-column: span 12;
        grid-template-columns: repeat(12, 1fr);
    }

    .header-channel-fixed-right-left {
        grid-column: span 9;
        grid-template-columns: repeat(9, 1fr);
    }

    .header-channel-fixed-right .left-bottom {
        grid-column: span 12;
        grid-template-columns: repeat(12, 1fr);
    }

    .header-channel-fixed-right-left .left-top {
        grid-column: span 9;
        grid-template-columns: repeat(9, 1fr);
    }

    .header-channel-fixed-right .left-bottom,
    .header-channel-fixed-right-right,
    .header-channel-fixed-right-left .left-top,
    .header-channel-fixed-right {
        font-size: 13px;
        grid-gap: 14px 8px;
    }

    .left-top .header-channel-fixed-right-item:nth-of-type(1n + 19) {
        display: none !important;
    }

    .left-bottom .header-channel-fixed-right-item:nth-of-type(-1n + 18) {
        display: none !important;
    }
}

@media (min-width: 1367px) and (max-width: 1700.9px) {
    .header-channel-fixed-right {
        grid-column: span 14;
        grid-template-columns: repeat(14, 1fr);
    }

    .header-channel-fixed-right-left {
        grid-column: span 11;
        grid-template-columns: repeat(11, 1fr);
    }

    .header-channel-fixed-right .left-bottom {
        grid-column: span 14;
        grid-template-columns: repeat(14, 1fr);
    }

    .header-channel-fixed-right-left .left-top {
        grid-column: span 11;
        grid-template-columns: repeat(11, 1fr);
    }

    .left-top .header-channel-fixed-right-item:nth-of-type(1n + 23) {
        display: none !important;
    }

    .left-bottom .header-channel-fixed-right-item:nth-of-type(-1n + 22) {
        display: none !important;
    }
}

@media (min-width: 1701px) and (max-width: 2199.9px) {
    .header-channel-fix {
        --left_width: 160px;
        --left_width_item: 80px;
    }

    .header-channel-fixed-right {
        grid-column: span 15;
        grid-template-columns: repeat(15, 1fr);
    }

    .header-channel-fixed-right-left {
        grid-column: span 12;
        grid-template-columns: repeat(12, 1fr);
    }

    .header-channel-fixed-right .left-bottom {
        grid-column: span 15;
        grid-template-columns: repeat(15, 1fr);
    }

    .header-channel-fixed-right-left .left-top {
        grid-column: span 12;
        grid-template-columns: repeat(12, 1fr);
    }

    .left-top .header-channel-fixed-right-item:nth-of-type(1n + 25) {
        display: none !important;
    }

    .left-bottom .header-channel-fixed-right-item:nth-of-type(-1n + 24) {
        display: none !important;
    }
}

@media (min-width: 2200px) {
    .header-channel-fix {
        --left_width: 160px;
        --left_width_item: 80px;
    }

    .header-channel-fixed-right {
        grid-column: span 17;
        grid-template-columns: repeat(17, 1fr);
    }

    .header-channel-fixed-right-left {
        grid-column: span 14;
        grid-template-columns: repeat(14, 1fr);
    }

    .header-channel-fixed-right .left-bottom {
        display: none;
    }

    .header-channel-fixed-right-left .left-top {
        grid-column: span 14;
        grid-template-columns: repeat(14, 1fr);
    }
}

.header-channel-fixed-arrow {
    position: absolute;
    right: calc(var(--layout-padding, 60px) - 40px);
    width: 28px;
    height: 28px;
    padding: 6px;
    margin-left: 10px;
    align-self: start;
    cursor: pointer;
    display: flex;
    align-items: center;
    border-radius: 4px;
    transition: .2s;
}

.header-channel-fixed-arrow:hover {
    background-color: var(--graph_bg_thick);
}

.icon-xiajiantou {
    font-weight: 600;
    width: 100%;
    height: 100%;
    transition: .2s;
}

.main__layout {
    background-color: #fff;
    margin: 0 auto;
    padding: 0 60px;
    max-width: calc(1980px + 2 * 60px);
    width: 100%;
}

@media (max-width: 1139.9px) {
    .main__layout {
        width: 1020px;
    }

    .flex-wapper {
        width: 1020px;
    }

}

@media (min-width: 1140px) {
    .main__layout {
        position: relative;
    }

    .recommended-container {
        position: relative;
    }
}

.recommended-container {
    padding-bottom: 60px;
}

.container {
    grid-gap: 20px;
    display: grid;
    position: relative;
    width: 100%;
}

@media (max-width: 1399.9px) {
    .container {
        grid-column: span 4;
        grid-template-columns: repeat(4, 1fr);
    }

    .container>*:nth-of-type(n + 6) {
        margin-top: 40px;
    }

    .container .feed-card:nth-of-type(n + 10) {
        display: none;
    }
}

@media (min-width: 1400px) {
    .container {
        grid-column: span 5;
        grid-template-columns: repeat(5, 1fr);
    }

    .container>*:nth-of-type(n + 8) {
        margin-top: 40px;
    }
}

.recommended-swipe {
    position: relative;
    grid-column: 1/3;
    /* 跨越从第1列到第3列，占据两个网格列的宽度，等价于 grid-column: span 2; */
    grid-row: 1/3;
    /* 跨越从第1行到第3行，占据两个网格行的高度 */
}

.recommended-swipe-core {
    position: relative;
    width: 100%;
}

.recommended-swipe-shim {
    opacity: 0;
    visibility: hidden;
    user-select: none;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    pointer-events: none;
    grid-column: span 2;
    grid-row: span 2;
    grid-template-columns: repeat(2, 1fr);
    grid-gap: 20px;
    width: 100%;
    display: grid;
}

.shim-card {
    width: 100%;
    height: 0;
    padding-top: 56.25%;
}

.recommended-swipe-body {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    transform: translateZ(0);
    /* 没有视觉效果的平移，它可以触发 GPU 加速，以提高性能和动画平滑度 */
    border-radius: 6px;
    overflow: hidden;
    background-color: var(--graph_bg_regular);
}

.feed-roll-btn {
    left: 100%;
    top: 0;
    z-index: 2;
}

.feed-roll-btn .roll-btn {
    flex-direction: row;
    margin-left: 0;
    height: auto;
    width: 90px;
    padding: 8px 12px;
    background-color: #fff;
    color: var(--text1);
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
    border-radius: 8px;
    font-size: 12px;
    border: 1px solid var(--line_regular);
    transform-origin: center;
    transition: .2s;
    cursor: pointer;
    line-height: 1.15;
}

.feed-roll-btn .roll-btn:hover {
    background-color: var(--graph_bg_thick);
}

.feed-roll-btn .roll-btn .iconfont {
    line-height: 16px;
    margin-right: 6px;
    transition: transform .5s ease;
}

@media (max-width: 1120px) {
    .feed-roll-btn {
        opacity: 0.8;
    }
}

.flex-wapper {
    position: sticky;
    height: var(--bili_header_height);
    top: 0px;
    overflow: hidden;
    z-index: 1000;
    background-color: #fff;
    margin-bottom: 16px;
    border-bottom: 1px solid #ddd;
}

.channel-nav {
    width: 100%;
    display: flex;
    height: var(--bili_header_height);
    align-items: center;
    line-height: var(--bili_header_height);
    position: relative;
    z-index: 1;
}

.channel-nav-name {
    font-size: 24px;
    line-height: 34px;
    min-width: 48px;
    height: 34px;
    margin-right: 25px;
    color: #18191C;
    cursor: pointer;
}

.video-grid {
    position: relative;
    margin-bottom: 56px;
    width: 100%;
}

.video-card-list {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    grid-gap: 20px;
    min-height: 280px;
    position: relative;
}

.aera-header{
    position: relative;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    z-index: 2;
    height: 34px;
    margin-bottom: 16px;
    font-size: 22px;
}

.area-header .left {
    height: 100%;
    display: flex;
    flex-direction: row;
    align-items: center;
}

.area-header .right{
    height: 100%;
    display: flex;
    flex-direction: row;
    align-items: center;
}

.feed-card {
    background-color: var(--graph_bg_regular);
    border-radius: 6px;
    overflow: hidden;
}

.empty-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
}
</style>