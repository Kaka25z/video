<template>
    <div class="s-space">
        <div class="col-full">
            <div class="main-content">
                <div class="header">
                    <div class="text">
                        <p>全部关注</p>
                    </div>
                </div>
                <div class="content">
                    <div class="follow-list">
                        <div v-for="(item, index) in paginatedUsers" :key="index" class="follow-item">
                            <div class="img-card">
                                <VPopover popStyle="z-index: 2000; cursor: default; padding-top: 30px;">
                                    <template #reference>
                                        <VAvatar :img="item.avatar_url" :size="60" :auth="item.auth"></VAvatar>
                                    </template>
                                    
                                    <template #content>
                                        <UserCard :user="item"></UserCard>
                                    </template>
                                </VPopover>

                            </div>
                            <div class="follow-content">
                                <p class="title">{{ item.nickname }}</p>
                                <p class="desc">{{ item.description }}</p>
                            </div>
                            <div class="up-info__btn-panel">
                                <div class="default-btn follow-btn not-follow" v-if="!followStatus[item.uid]">
                                    <i class="iconfont icon-jia"></i>
                                    关注
                                </div>
                                <VPopover popStyle="padding-top: 10px;">

                                    <template #reference>
                                        <div class="default-btn follow-btn following" v-if="followStatus[item.uid]">
                                            <i class="iconfont icon-caidan"></i>
                                            已关注
                                        </div>
                                    </template>

                                    <template #content>
                                        <div class="following-dropdown">
                                            <div class="dropdown-item">
                                                <span>设置分组</span>
                                            </div>
                                            <div class="dropdown-item">
                                                <span>取消关注</span>
                                            </div>
                                        </div>
                                    </template>
                                </VPopover>
                                <VPopover popStyle="padding-top: 10px;">

                                    <template #content>
                                        <div class="following-dropdown">
                                            <div class="dropdown-item">
                                                <span>设置分组</span>
                                            </div>
                                            <div class="dropdown-item">
                                                <span>取消关注</span>
                                            </div>
                                        </div>
                                    </template>
                                </VPopover>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="pagination">
                    <el-pagination
                        background
                        layout="prev, pager, next"
                        :total="totalUsers"
                        :page-size="itemsPerPage"
                        @current-change="handlePageChange">
                    </el-pagination>
                </div>
            </div>
        </div>
    </div>
</template>

<script>

import { ElPagination } from 'element-plus';
import VPopover from '@/components/popover/VPopover.vue';
import VAvatar from '@/components/avatar/VAvatar.vue';
import UserCard from '@/components/UserCard/UserCard.vue';

export default {
    name: "SpaceFollow",
    data() {
        return {
            followList: [],
            spaceId: null,
            userInfo: [],
            followStatus: {},
            currentPage: 1,
            itemsPerPage: 20,
        }
    },
    components: {
        VPopover,
        VAvatar,
        UserCard,
        ElPagination
    },
    computed: {
        paginatedUsers() {
            const start = (this.currentPage - 1) * this.itemsPerPage;
            const end = start + this.itemsPerPage;
            return this.userInfo ? this.userInfo.slice(start, end) : [];
        },
        totalUsers() {
            return this.userInfo ? this.userInfo.length : 0;
        }
    },
    methods:{
        async getFollowList(uid) {
            const res = await this.$get("/user/follow/get-all", {
                params: {
                    uid: uid
                },
                headers: { Authorization: "Bearer " + localStorage.getItem("token") }
            });

            this.followList = res.data.data.followUids;
        },

        async getUserInfo(uid) {
            const res = await this.$get("/user/info/get-list", {
                params: {
                    uid: uid
                },
                headers: { Authorization: "Bearer " + localStorage.getItem("token") }
            });

            this.userInfo = res.data.data;

            this.userInfo.forEach(user => {
                this.followStatus[user.uid] = true;
            })
        },

        handlePageChange(page) {
            this.currentPage = page;
        }
    },
    async mounted() {

        this.spaceId = this.$route.params.spaceId || this.$route.path.split('/')[2];

        if (this.spaceId == this.$store.state.user.uid) {
            await this.getFollowList(this.$store.state.user.uid);
        } else {
            await this.getFollowList(this.spaceId);
        }
    },
    watch: {
        async followList(newList) {
            if (newList.length > 0) {
                await this.getUserInfo(newList);
            }
        }
    }
}
</script>

<style scoped>

.user-item {
    margin-bottom: 20px;
}

.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 20px;
}

.main-content {
    float: left;
    padding: 20px;
    box-sizing: border-box;
    border: 1px solid #eee;
    min-height: 600px;
    width: 1101px;
    border-radius: 4px;
    background: white
}

.header {
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;
    margin-top: 20px;
    margin-left: 20px;
    margin-right: 20px;
    height: 30px;
    width: 95%;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.text {
    font-size: 18px;
    color: rgb(34, 34, 34);
    font-weight: 0;
    line-height: 30px;
    display: inline-block;
    justify-content: space-between;
}

.content {
    padding-bottom: 10px;
    width: 100%;
}

.follow-list {
    display: flex;
    justify-content: space-between;
    flex-direction: column;
    width: 100%;
    padding-left: 20px;
    padding-right: 20px;
}

.follow-item {
    border-bottom: 1px solid #eee;
    height: 102px;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.follow-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: flex-start;
    padding-left: 20px;
    position: absolute;
    left: 108px;
}

.title {
    font-size: 16px;
    color: rgb(34, 34, 34);
    font-weight: 0;
    line-height: 19px;
    height: 20px;
    margin-bottom: 10px;
}

.desc {
    font-size: 12px;
    color: rgb(109, 117, 122);
    font-weight: 0;
    line-height: 20px;
    height: 20px;
}

.up-info__btn-panel {
    clear: both;
    display: flex;
    margin-top: 5px;
    white-space: nowrap;
}

.up-info__btn-panel .default-btn {
    box-sizing: border-box;
    padding: 0;
    line-height: 16px;
    height: 24px;
    border-radius: 6px;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    background: var(--graph_weak);
    position: relative;
    transition: 0.3s all;
}

.follow-btn {
    width: 74px;
}

.follow-btn.following {
    color: var(--text3);
    background-color: var(--graph_bg_thick);
}

.follow-btn.following:hover {
    background-color: var(--graph_bg_regular);
}

.follow-btn.not-follow {
    background: var(--brand_pink);
    color: var(--text_white);
}

.follow-btn.not-follow:hover {
    background: var(--Pi4);
}

.follow-btn .iconfont {
    font-size: 14px;
    margin-right: 2px;
}

.following-dropdown {
    padding: 8px 0px;
}

.following-dropdown .dropdown-item:hover {
    color: var(--brand_pink);
}

.dropdown-item {
    position: relative;
    display: flex;
    align-items: center;
    height: 40px;
    width: 120px;
    padding: 0 20px;
    color: var(--text1);
    cursor: pointer;
}

.dropdown-item:hover {
    background-color: var(--Ga1);
}

@media (min-width: 1420px) {
    .main-content {
        width: 1284px;
    }
}

</style>