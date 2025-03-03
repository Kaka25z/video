<template>
    <div class="flex-fill">
        <div class="container-v">
            <div class="v-card" style="border-radius: 15px;">
                <NavBar :navBarItem="navBarData"></NavBar>
                <el-table 
                    :data="paginatedCommentInfo"
                    style="
                    width: 100%;
                    z-index: 0;
                    border-radius: 15px;
                    background-color: white;
                    padding: 20px;
                    "
                    table-layout="auto"
                    size="large"
                >
                    <el-table-column fixed prop="comment.id" label="评论ID"></el-table-column>
                    <el-table-column prop="comment.content" label="评论内容">
                        <template v-slot="scope">
                            <span v-html="formatContent(scope.row.comment.content)"></span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="comment.createTime" label="评论时间" width="200">
                        <template v-slot="scope">
                            {{ (scope.row.comment.createTime) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="videoTitle" label="来源视频标题"></el-table-column>
                    <el-table-column prop="isDeleted" label="评论状态" width="100">
                        <template v-slot="scope">
                            <el-tag v-if="scope.row.comment.isDeleted === 0" type="warning" class="tag">正常</el-tag>
                            <el-tag v-else-if="scope.row.comment.isDeleted === 1" type="danger" class="tag">已删除</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="user" label="评论者"></el-table-column>
                    <el-table-column prop="userAvatarUrl" label="评论者头像">
                        <template v-slot="scope">
                            <img :src="scope.row.userAvatarUrl" style="width: 50px; height: 50px; border-radius: 50%">
                        </template>
                    </el-table-column>
                    <el-table-column fixed="right" label="操作" width="200">
                        <template v-slot="scope">
                            <el-button 
                                link
                                type="danger"
                                @click="handleDelete(scope.row)"
                                :disabled="scope.row.comment.isDeleted === 1"
                            >
                                删除
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="footer">
                    <el-pagination
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange"
                        v-model:current-page="currentPage"
                        :page-sizes="[10, 20, 30, 40]"
                        v-model:page-size="pageSize"
                        :total="commentInfo.length"
                        layout="prev, pager, next, sizes"
                    >
                    </el-pagination>
                </div>

            </div>
        </div>
    </div>
</template>

<script>
import NavBar from "@/components/navbar/NavBar.vue";
import { emojiText } from "@/utils/utils";

export default {
    name: "CommentManage",
    components: {
        NavBar
    },
    data() {
        return {
            navBarData: [
                {
                    name: "评论管理",
                }
            ],
            commentInfo: [],
            currentPage: 1,
            pageSize: 10,
        }
    },
    computed: {
        paginatedCommentInfo() {
            const start = (this.currentPage - 1) * this.pageSize;
            const end = start + this.pageSize;
            return this.commentInfo.slice(start, end);
        }
    },
    methods: {
        async fetchCommentInfo() {

            const res = await this.$get("/comment/get-all", {
                params: {
                    page: this.currentPage,
                    quantity: this.pageSize
                },
                headers: { Authorization: "Bearer " + localStorage.getItem("token"), },
            });

            if (res.data.code === 200) {
                this.commentInfo = res.data.data;
                this.totalComments = res.data.data.length;
                console.log(this.commentInfo);
            } else {
                this.$message.error(res.message);
            }
        },

        handleSizeChange(size) {
            this.pageSize = size;
            this.currentPage = 1; // 重置到第一页
            this.fetchCommentInfo();
        },

        handleCurrentChange(page) {
            this.currentPage = page;
            this.fetchCommentInfo();
        },

        formatLocalTime(timestamp) {
            const date = new Date(timestamp);
            return date.toLocaleString();
        },

        formatContent(content) {
            return emojiText(content);
        },

        handleDelete(row) {
            this.$confirm("确认删除该评论吗？", "提示", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning",
            })
                .then(async () => {

                    const formData = new FormData();

                    formData.append("id", row.comment.id);

                    const res = await this.$post("/comment/delete", formData , {
                        headers: { Authorization: "Bearer " + localStorage.getItem("token"), },
                    });

                    if (res.data.code === 200) {
                        this.$message.success("删除成功");
                        this.fetchCommentInfo();
                    }
                })
                .catch(() => {
                    this.$message.info("已取消删除");
                });
        }
    },
    mounted() {
        this.fetchCommentInfo();
    }
}

</script>

<style scoped>
.container-v {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    margin-left: 26px;
    margin-right: 26px;
    padding: 16px;
}

.footer {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 20px;
    margin-bottom: 16px;
}

.tag {
    padding: 5px;
    margin: 2px;
    font-size: 14px;
}
</style>