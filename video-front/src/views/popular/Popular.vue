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
        </div>

        <div class="main__layout">
            <div class="nav-tabs">
                <div class="channel-icons__item">
                    <div class="icon-bg icon-bg__popular">
                        <i class="iconfont icon-huo"></i>
                    </div>
                    <span class="icon-title">综合热门</span>
                </div>
            </div>
            <div class="popular-video">
                <div class="video-card" v-for="popularList in popularList" :key="popularList.rank">
                    <div class="video-card-content">
                        <a :href="`/video/${popularList.video.vid}`" target="_blank">
                            <img :src="popularList.video.coverUrl" />
                        </a>
                    </div>
                    <div class="video-card-info">
                        <p class="video-name">
                            <a :href="`/video/${popularList.video.vid}`" target="_blank">
                                {{ popularList.video.title }}
                            </a>
                        </p>
                        <div class="video-data">
                            <span class="upname">
                                <a :href="`/space/${popularList.user.uid}`" target="_blank">
                                    <i class="iconfont icon-uper" style="width: 18px; height: 18px;"></i>
                                    <span class="upname-text">
                                        {{ popularList.user.nickname }}
                                    </span>
                                </a>
                            </span>
                            <div class="video-stat">
                                <span class="play-text">
                                    <i class="iconfont icon-bofangshu" style="width: 18px; height: 18px;"></i>
                                    {{ handleNum(popularList.stats.play) }}
                                </span>
                                <span class="danmu-text">
                                    <i class="iconfont icon-danmushu" style="width: 18px; height: 18px;"></i>
                                    {{ handleNum(popularList.stats.danmu) }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import HeaderBar from '@/components/headerBar/HeaderBar.vue';
import { handleNum } from '@/utils/utils.js';

export default{
    name: "PopulrIndex",
    components: {
        HeaderBar,
    },
    data() {
        return {
            isFixHeaderBar: false,
            popularList: [],
        }
    },
    methods: {
        async getPopularList() {
            const res = await this.$get('/video/ranked-details', {
                params: {
                    start: 0,
                    end: 99
                }
            });
            if (res.data.code === 200) {
                this.popularList = res.data.data;
                console.log(this.popularList);
            }
        },

        handleNum(number) {
            return handleNum(number);
        },
    },
    mounted() {
        this.getPopularList();
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
    padding: 0 0 80px;
    min-width: 107px;
    max-width: 1286px;
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

.icon-bg {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 10px;
    border-radius: 50%;
    transition: background-color 0.3s;
    color: #fff;
    width: 36px;
    height: 36px;
}

.icon-bg__popular {
    background: #f07775;
}

.nav-tabs {
    width: 100%;
    height: 84px;
    border-bottom: 1px solid #ddd;
}

.icon-title {
    text-align: center;
    font-size: 16px;
    line-height: 18px;
}

.channel-icons__item {
    display: flex;
    height: 83px;
    justify-content: flex-start;
    align-items: center;
    width: 115px;
    border-bottom: 2px solid var(--brand_blue, #00aeec);
}

.popular-video {
    padding-top: 40px;
    display: flex;
    flex-wrap: wrap;
}

.video-card {
    width: calc(50% - 10px);
    margin-right: 10px;
    margin-bottom: 40px;
    display: flex;
}

.video-card-content {
    width: 206px;
    flex-shrink: 0;
    margin-right: 10px;
    background: #e7e7e7;
    height: 116px;
    border-radius: 2px;
    position: relative;
    box-sizing: border-box;
}

.video-card-content img {
    width: 100%;
    height: 100%;
    border-radius: 2px;
}

.video-card-info {
    cursor: pointer;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-content: center;
    justify-content: space-between;
    color: #9499a0;
    color: var(--text3, #9499a0);
    font-size: 12px;
    padding: 0 70px 0 0;
}

.video-name {
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    text-overflow: -o-ellipsis-lastline;
    text-overflow: ellipsis;
    word-break: break-word;
    -webkit-line-clamp: 2;
    height: 43px;
    font-size: 14px;
    color: #18191c;
    color: var(--text1, #18191c);
    padding: 0;
    margin-bottom: 8px;
    line-height: 20px;
    transition: color .3s;
    font-weight: 600;
}

.upname {
    display: flex !important;
    align-items: center;
    height: 16px;
    font-size: 12px;
    color: #9499a0;
    color: var(#9499A0, #9499a0);
    margin-bottom: 4px;
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    text-overflow: -o-ellipsis-lastline;
    text-overflow: ellipsis;
    word-break: break-word;
    -webkit-line-clamp: 1;
}

.upname-text {
    font-size: 12px;
    color: var(#9499A0, #9499a0);
}

.video-stat {
    line-height: 16px;
    display: flex;
    align-items: center;
}

.play-text {
    margin-right: 12px;
    display: flex;
    align-items: center;
}

.danmu-text {
    display: flex;
    align-items: center;
}
</style>