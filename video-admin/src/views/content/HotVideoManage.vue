<template>
    <div class="flex-fill">
        <div class="container-v">
            <div class="v-card" style="border-radius: 15px;">
                <NavBar :navBarItem="navBarData"></NavBar>
                <div class=main>
                    <el-table
                        :data="videoDetails"
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
                        <el-table-column
                            prop="score"
                            label="热度"
                            fixed
                        ></el-table-column>
                        <el-table-column
                            prop="vid"
                            label="视频ID"
                            width="100"
                        ></el-table-column>
                        <el-table-column
                            prop="video.coverUrl"
                            label="视频封面"
                            width="250"
                        >
                            <template v-slot="scope">
                            <img :src="scope.row.video.coverUrl" alt="封面" style="width: 250px; height: auto; border-radius: 10px">
                            </template>
                        </el-table-column>
                        <el-table-column
                            prop="video.title"
                            label="标题"
                        ></el-table-column>
                        <el-table-column
                            prop="video.descr"
                            label="视频简介"
                            width="500"
                        ></el-table-column>
                        <el-table-column
                            prop="user.nickname"
                            label="作者"
                        ></el-table-column>
                        <el-table-column
                            prop="video.uploadDate"
                            label="上传时间"
                            width="150"
                        ></el-table-column>
                        <el-table-column
                            label="操作"
                            width="200"
                            fixed="right"
                        >
                            <template v-slot="scope">
                                <el-button
                                    type="primary"
                                    size="default"
                                    plain
                                    style="width: 80px;"
                                    @click="handleEdit(scope.row)"
                                >
                                    修改分数
                                </el-button>
                                <el-button
                                    type="danger"
                                    size="default"
                                    plain
                                    style="width: 40px;"
                                    @click="handleDelete(scope.row)"
                                >
                                    删除
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>
        </div>
    </div>

    <el-dialog
        title="修改分数"
        v-model="editDialogVisible"
        align-center
        style="border-radius: 15px; padding: 24px; width: 400px;"
    >
        <el-input v-model="newScore" placeholder="请输入新的分数" style="margin-bottom: 16px; margin-top: 12px;" class="dialog"></el-input>
        <template v-slot:footer>
            <el-button @click="editDialogVisible = false" style="width: 50px; margin-right: 12px;">取消</el-button>
            <el-button type="primary" @click="confirmEdit" style="width: 50px;">确定</el-button>
        </template>
    </el-dialog>

    <el-dialog
        title="确认删除"
        v-model="deleteDialogVisible"
        align-center
        style="border-radius: 15px; padding: 24px; width: 400px;"
    >
        <p style="margin-top: 12px; margin-bottom: 16px;">确定要删除该热门视频吗？</p>
        <template v-slot:footer>
            <el-button @click="deleteDialogVisible = false" style="width: 50px; margin-right: 12px;">取消</el-button>
            <el-button type="danger" @click="confirmDelete" style="width: 50px;">删除</el-button>
        </template>
    </el-dialog>
</template>

<script>
import NavBar from "@/components/navbar/NavBar.vue";

export default {
    name: "HotVideo",
    components: {
        NavBar
    },
    data() {
        return {
            navBarData: [
                { name: "热门视频管理" },
            ],
            hotVideo:[],
            deleteDialogVisible: false,
            editDialogVisible: false,
            currentRow: null,
            currentPage: 1,
            pageSize: 10,
            videoDetails: [],
            newScore: null
        };
    },
    methods: {
        async getHotVideo() {
            const res = await this.$get("/video/score", {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token")
                }
            })
            this.hotVideo = res.data.data
            console.log(this.hotVideo)

            if (this.hotVideo.length > 0) {
                this.fetchVideoDetails();
            }
        },

        async fetchVideoDetails() {
            const vidList = this.hotVideo.map(video => video.vid).join(',');
            const res = await this.$get("/video/details-by-vids", {
                params: {
                    vids: vidList,
                    page: this.currentPage,
                    quantity: this.pageSize
                },
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token")
                }
            });

            console.log("vidlist" + vidList);

            if (res.data.code === 200) {
                const videoDetails = res.data.data;
                this.videoDetails = videoDetails.map(detail => {
                    const hotVideo = this.hotVideo.find(video => video.vid === detail.video.vid);
                    return {
                        ...detail,
                        score: hotVideo ? hotVideo.score : null,
                        vid: detail.video.vid,
                    };
                });
                console.log(this.videoDetails);
            } else {
                this.$message.error(res.message);
            }
        },

        handleDelete(row) {
            this.currentRow = row;
            this.deleteDialogVisible = true;
        },

        handleEdit(row) {
            this.currentRow = row;
            this.newScore = null;
            this.editDialogVisible = true;
        },

        async confirmEdit() {
            const res = await this.$post("/video/update-score", null, {
                params: {
                    vid: this.currentRow.vid,
                    newScore: this.newScore
                },
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token")
                }
            });

            if (res.data.code === 200) {
                this.$message({
                    message: "修改成功",
                    type: "success"
                });
                this.getHotVideo();
            } else {
                this.$message({
                    message: "修改失败",
                    type: "error"
                });
            }
            this.editDialogVisible = false;
        },

        async confirmDelete() {
            const formData = new FormData();
            formData.append('vid', this.currentRow.vid);

            const res = await this.$post("/video/delete-score", formData, {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token")
                }
            });

            if (res.data.code === 200) {
                this.$message({
                    message: "删除成功",
                    type: "success"
                });
                this.getHotVideo();
            } else {
                this.$message({
                    message: "删除失败",
                    type: "error"
                });
            }
            this.deleteDialogVisible = false;
        }
    },
    mounted() {
        this.getHotVideo()
    }
}
</script>

<style scoped>
.main {
    padding: 40px;
    width: 100%;
}

.dialog-footer {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}

.container-v {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    margin-left: 26px;
    margin-right: 26px;
    padding: 16px;
}

.dialog .el-input__inner {
    padding: 5px;
}

</style>