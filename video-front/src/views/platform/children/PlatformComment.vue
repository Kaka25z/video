<template>
    <div class="platform-comment">
        <NavBar :navBarItem="navBarData"></NavBar>
        <div class="flex-fill">
            <div class="comment-content">
                <div class="comment-item" v-for="(item, index) in commentList" :key="index">
                    <div style="padding-top: 15px">
                        <el-avatar :size=40 :src="item.userAvatarUrl" alt="" />
                    </div>
                    <div class="comment-info">
                        <div class="comment-user">{{ item.user }}</div>
                        <div class="comment-text">{{ item.comment.content }}</div>
                        <div class="time-container" @mouseover="showDelete = true" @mouseleave="showDelete = false">
                            <p class="time">{{ formatDate(item.comment.createTime) }}</p>
                            <button v-if="showDelete" @click="deleteComment(item.comment.id)" class="delete-button">删除</button>
                        </div>
                    </div>
                    <div class="video-info">
                        <img :src="item.videoCoverUrl" />
                        <p>{{ truncatedTitle(item.videoTitle) }}</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import NavBar from '@/components/navbar/NavBar.vue';

export default {
    name: "PlatformComment",
    components: {
        NavBar,
    },
    data() {
        return {
            navBarData: [
                { name: "评论管理", path: '/platform/comment'},
            ],
            commentList: [],
            page: 1,
            quantity: 10,
            showDelete: false,
            deleteCommentId: null,
        }
    },
    methods: {
        async getCommentList() {
            const res = await this.$get("/comment/get-by-uid", {
                params: {
                    uid: this.$store.state.user.uid,
                    page: this.page,
                    quantity: this.quantity,
                },
                headers: { Authorization: "Bearer " + localStorage.getItem("token") }
            });

            if (!res.data) return;

            this.commentList = res.data.data;
            console.log(this.commentList)
        },

        formatDate(timestamp) {
            const date = new Date(timestamp);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            const seconds = String(date.getSeconds()).padStart(2, '0');
            return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
        },

        truncatedTitle(title) {
            return title.length > 7 ? title.substring(0, 7) + '...' : title;
        },

        async submitDelete() {

            const formData = new FormData();

            formData.append("id", this.deleteCommentId);

            await this.$post("/comment/delete", formData,{
                headers: { Authorization: "Bearer " + localStorage.getItem("token") },
            }).then(() => {
                this.getCommentList();
                this.$message({
                    type: 'success',
                    message: '删除成功'
                });
            }).catch(() => {
                this.$message({
                    type: 'error',
                    message: '删除失败'
                });
            });
        },

        deleteComment(id) {
            this.deleteCommentId = id;
            this.$confirm('确定删除该评论吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.submitDelete();
            }).catch(() => {
                this.deleteCommentId = null;
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });
        }
    },
    mounted() {
        this.getCommentList();
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

.comment-content {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    padding: 32px;
    border-spacing: 0 10px;
    border-collapse: separate;
}

.comment-item {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
}

.comment-info {
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: flex-start;
    padding-left: 20px;
}

.comment-text {
    width: 100%;
    padding-top: 10px;
    font-size: 14px;
    margin-bottom: 24px;
}

.comment-user {
    width: 100%;
    padding-top: 10px;
    font-size: 14px;
    color: rgb(102, 102, 102);
    margin-bottom: 12px;
}

.time {
    font-size: 14px;
    color: #999;
    width: 80%;
    display: inline-block;
}

.time-container {
    position: relative;
    display: inline-block;
    width: 20%;
}

.time-container:hover .delete-button {
    display: inline-block;
}

.delete-button {
    display: none;
    position: absolute;
    right: 0;
    top: 0;
    color: #999;
    border: none;
    cursor: pointer;
    background-color: transparent;
}

.delete-button:hover {
    color: rgb(255, 102, 153)
}

.video-info {
    width: 200px;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    flex-direction: column;
    padding-top: 10px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}

.video-info p {
    font-size: 12px;
    color: #666;
}

.video-info img {
    width: 100px;
    height: 60px;
    margin-bottom: 10px;
    border-radius: 4px;
}

</style>