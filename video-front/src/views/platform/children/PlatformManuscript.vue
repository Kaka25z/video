<template>
    <div class="platform-manuscript">
        <NavBar :navBarItem="navBarData" @clickBarItem="clickBarItem" :style="isNavBarShow ? '' : 'display: none;'"></NavBar>
        <div class="flex-fill" :style="isNavBarShow ? '' : 'display: none;'" >
            <div class="v-card">
                <div class="video-table-card">
                    <div class="v-table" v-loading="loading">
                        <div class="v-table__wrapper">
                            <div class="text-tab">全部稿件 {{videoList.length}}</div>
                            <table class="custom-table">
                                <tbody v-if="videoList.length != 0" >
                                    <tr v-for="(item, index) in videoList" :key="index" class="tr-card">
                                        <td style="width: 176px;">
                                            <img :src="item.video.coverUrl" class="cover" alt="">
                                        </td>
                                        <td>
                                            <div class="video-title">
                                                {{ item.video.title }}
                                            </div>
                                            <div class=mid>
                                                <div class="video-date">
                                                    {{ item.video.uploadDate }}
                                                </div>
                                                <div class="video-status">
                                                    <p style="margin-bottom: 5px">审核状态</p>
                                                    <el-text v-if="item.video.status == 1" type="success">已通过</el-text>
                                                    <el-text v-else-if="item.video.status == 2" type="danger">未通过</el-text>
                                                    <el-text v-else type="warning">审核中</el-text>
                                                </div>
                                                <div>
                                                    <el-button :icon="Edit" @click="clickBarItem(item.video.vid)">编辑</el-button>
                                                    <el-button type="danger" :icon="Delete">删除</el-button>
                                                </div>
                                            </div>
                                            <div class="footer">
                                                <div class="video-state">
                                                    <img src="~/assets/img/icon/play.png" alt="">
                                                    <span>{{ item.stats.play }}</span>
                                                    <img src="~/assets/img/icon/good.png" alt="">
                                                    <span>{{ item.stats.good }}</span>
                                                    <img src="~/assets/img/icon/danmu.png" alt="">
                                                    <span>{{ item.stats.danmu }}</span>
                                                    <img src="~/assets/img/icon/comment.png" alt="">
                                                    <span>{{ item.stats.comment }}</span>
                                                    <img src="~/assets/img/icon/coin.png" alt="">
                                                    <span>{{ item.stats.coin }}</span>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>            
        </div>
        <router-view @changeNavBarShow="changeNavBarShow"></router-view>
    </div>
</template>

<script>
import NavBar from '@/components/navbar/NavBar.vue';
import {
    Delete,
    Edit,
} from '@element-plus/icons-vue'

export default {
    name: "PlatformManuscript",
    computed: {
        Delete() {
            return Delete
        },
        Edit() {
            return Edit
        }
    },
    components: {
        NavBar,
    },
    data() {
        return {
            isNavBarShow: true,
            isEditing: false,
            navBarData: [
                { name: "视频管理", path: '/platform/edit/video'},
            ],
            rule: 1,
            page: 1,
            videoList: [],
            vid: null,
        }
    },
    methods: {
        async getVideoList() {
            const res = await this.$get("/video/data/get-by-vids", {
                params: {
                    uid: this.$store.state.user.uid,
                },
                headers: { Authorization: "Bearer " + localStorage.getItem("token") }
            });
            if (!res.data) return;
            this.videoList = res.data.data.list;
            console.log(this.videoList)
        },
        clickBarItem(vid) {
            console.log(vid)
            this.isEditing = true;
            this.isNavBarShow = false;
            this.vid = vid;
            this.$router.push(`/platform/edit/video/${vid}`);
        },
        changeNavBarShow(flag) {
            this.isNavBarShow = flag;
        }
    },
    mounted() {
        this.getVideoList();
    }
}
</script>

<style scoped>
.video-table-card {
    height: calc(100vh - 96px);
    position: relative;
    overflow: hidden !important;
    overflow-anchor: none;
    -ms-overflow-style: none;
    touch-action: auto;
    -ms-touch-action: auto;
    padding: 0 40px 20px 40px;
}

.v-table {
    --v-table-row-height: 120px;
}

.v-table__wrapper {
    height: calc(100% - 150px);
}

.v-table__wrapper table {
    padding: 0 4px 8px;
}

.cover {
    height: 87px;
    width: 154px;
    object-fit: cover;
    box-shadow: 2px 2px 8px #0000001f;
    margin-right: 24px;
    border-radius: 4px;
}

.text-tab {
    font-size: 14px;
    font-weight: 400;
    color: rgb(255, 102, 153);
    margin-top: 16px;
    margin-bottom: 10px;
}

.video-title{
    font-size: 16px;
    font-weight: 400;
    color: rgb(29, 48, 54);

}

.mid {
    display: flex;
    justify-content: space-between;
}

.mid .video-date {
    font-size: 14px;
    font-weight: 400;
    color: rgb(80, 80, 80);
    padding-top: 18px;
    padding-bottom: 16px;
    margin-right: 4px;
}

.footer {
    display: flex;
    justify-content: space-between;
    height: 18px;
}

.video-state {
    display: flex;
    align-items: center;
    font-size: 14px;
    font-weight: 400;
    height: 100%;
    color: rgb(80, 80, 80);
    margin-right: 25px;
}

.footer .video-state img {
    height: 16px;
    width: 16px;
    margin-right: 4px;
}

.footer .video-state span {
    margin-right: 25px;
}

.edit-content {
    padding: 2px 30px;
}

.info-header {
    position: relative;
    font-size: 16px;
    font-weight: 600;
    color: #212121;
    line-height: 28px;
    height: 70px;
    box-shadow: 0 1px 0 #e7e7e7;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.cancel-btn {
    font-size: 14px;
    line-height: 1;
    padding: 12px 16px;
    color: var(--text2);
    cursor: pointer;
    border-radius: 4px;
    box-sizing: border-box;
    display: inline-block;
    text-align: center;
    border: 1px solid #e7e7e7;
    background-color: #fff;
    margin-right: 30px;
}

.cancel-btn:hover {
    background-color: #f4f4f4;
}

.custom-table {
    width: 95%;
    padding: 24px 0;
    border-spacing: 0 10px;
    border-collapse: separate;
}

.tr-card td{
    padding-bottom: 24px;
    padding-top: 12px;
    border-bottom: 1px solid #e7e7e7;
    background-color: #fff;
}

.tr-card:not(:last-child)::after {
    content: "";
    display: block;
    height: 1px;
    background-color: #e7e7e7;
    margin: 24px 0;
}

.video-status {
    font-size: 14px;
    font-weight: 400;
    color: rgb(80, 80, 80);
    display: flex;
    align-items: center;
    flex-direction: column;
}

</style>