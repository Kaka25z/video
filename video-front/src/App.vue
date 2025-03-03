<template>
    <div id="app">
        <router-view></router-view>
    </div>
</template>

<script>
import { ElLoading } from "element-plus";

export default {
    name: "App",
    components: {
    },
    data() {
        return {
            loadingInstance: null,
        }
    },
    methods: {
        // 获取频道列表
        async getChannels() {
            let res = await this.$get("/category/getall");
            console.log("频道列表: ", res);
            this.$store.commit("updateChannels", res.data.data);
        },

        async getHotSearch() {
            const res = await this.$get("/search/hot/get");
            this.$store.commit("updateTrendings", res.data.data);
        },

        showLoading() {
            this.loadingInstance = ElLoading.service({
                lock: true,
                text: '加载中...',
                background: 'rgba(0, 0, 0, 0.7)',
            });
        },

        
        async getFollowData(uid) {
            const res = await this.$get("/user/follow/get-all",{
                params: { 
                    uid: uid,
                },
                headers: { Authorization: "Bearer " + localStorage.getItem("token") }
            });
            this.$store.commit("updateFollowData", res.data.data);
        },

        // 隐藏加载蒙版
        hideLoading() {
            if (this.loadingInstance) {
                this.loadingInstance.close();
            }
        },

        // 开启实时通信消息服务
        async initIMServer() {
            await this.$store.dispatch("connectWebSocket");
            const connection = JSON.stringify({
                code: 100,
                content: "Bearer " + localStorage.getItem('token'),
            });
            this.$store.state.ws.send(connection);
        },

        // 关闭websocket
        async closeIMWebSocket() {
            await this.$store.dispatch("closeWebSocket");
        },

        // 获取当前用户的收藏夹列表
        async getFavorites() {
            const res = await this.$get("/favorite/get-all/user", {
                params: { uid: this.$store.state.user.uid },
                headers: { Authorization: "Bearer " + localStorage.getItem("token") }
            });
            if (!res.data) return;
            // 将默认置顶
            const defaultFav = res.data.data.find(item => item.type === 1);
            const list = res.data.data.filter(item => item.type !== 1);
            list.unshift(defaultFav);
            this.$store.commit("updateFavorites", list);
        },

        // 获取用户赞踩的评论集合
        async getLikeAndDisLikeComment() {
            const res = await this.$get("/comment/get-like-and-dislike", {
                params: { uid: this.$store.state.user.uid },
                headers: { Authorization: "Bearer " + localStorage.getItem("token") }
            });
            if (!res.data) return;
            this.$store.commit("updateLikeComment", res.data.data.userLike);
            this.$store.commit("updateDislikeComment", res.data.data.userDislike);
        }

    },
    async created() {
        // 如果缓存中有token，尝试获取用户数据，并建立全双工通信
        if (localStorage.getItem("token")) {
            await this.$store.dispatch("getPersonalInfo");

        }
        // 有可能上面获取信息时token过期就清掉了 所以这里再做个存在判断
        if (localStorage.getItem("token")) {
            this.$store.dispatch("getMsgUnread");
            await this.initIMServer();
            await this.getFavorites();
            await this.getLikeAndDisLikeComment();
            await this.getFollowData(this.$store.state.user.uid);
        }
        this.getChannels();
        this.getHotSearch();
    },
    mounted() {
        window.addEventListener('beforeunload', this.closeIMWebSocket);
        document.title = "BiliMini";
    },
    async beforeUnmount() {
        await this.closeIMWebSocket();
        window.removeEventListener('beforeunload', this.closeIMWebSocket);
    },
    watch: {
        "$store.state.isLoading"(current) {
            if (current) {
                this.showLoading();
            } else {
                this.hideLoading();
            }
        }
    }
};
</script>

<style>
#app {
    margin: 0 auto;
    max-width: 2560px;
    background-color: var(--bg1);
}

.loading-mark {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 50000;
    user-select: none;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
}

.loading-box {
    display: flex;
    height: 100vh;
    width: 100vw;
    align-items: center;
    justify-content: center;
}

.loading-box img {
    max-height: 33vh;
    max-width: 33vw;
}

.hide {
    animation: fade-out 0.2s ease-out forwards;
}

.show {
    animation: fade-in 0.2s ease-out forwards;
}

/* 淡入动画 */
@keyframes fade-in {
    0% {
        opacity: 0;
        /* 初始状态透明 */
    }

    100% {
        opacity: 1;
        /* 最终状态不透明 */
    }
}

/* 淡出动画 */
@keyframes fade-out {
    0% {
        opacity: 1;
        /* 初始状态不透明 */
    }

    100% {
        opacity: 0;
        /* 最终状态透明 */
    }
}
</style>
