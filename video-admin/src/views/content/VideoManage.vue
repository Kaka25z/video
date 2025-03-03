<template>
    <div class="flex-fill">
        <div class="container-v">
            <div class="v-card" style="border-radius: 15px;">
                <NavBar :navBarItem="navBarData"></NavBar>
                <el-table 
                    :data="paginatedVideoInfo"
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
                    <el-table-column fixed prop="video.vid" label="视频VID"></el-table-column>
                    <el-table-column prop="video.title" label="标题"></el-table-column>
                    <el-table-column label="封面" width="250">
                        <template v-slot="scope">
                          <img :src="scope.row.video.coverUrl" alt="封面" style="width: 250px; height: auto; border-radius: 10px">
                        </template>
                      </el-table-column>
                    <el-table-column prop="video.type" label="类型">
                        <template v-slot="scope">
                            <el-tag v-if="scope.row.video.type === 1" type="success">自制</el-tag>
                            <el-tag v-else-if="scope.row.video.type === 2" type="info">转载</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="video.duration" label="时长" width="100" >
                        <template v-slot="scope">
                            {{ formatDuration(scope.row.video.duration) }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="user.nickname" label="作者"></el-table-column>
                    <el-table-column prop="category" label="分区" width="200">
                        <template v-slot="scope">
                            <span class="category" style="background-color: #ffd024;">{{ scope.row.category.mcName }}</span> → 
                            <span class="category" style="background-color: #3ad2f0;">{{ scope.row.category.scName }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="video.tags" label="标签">
                        <template v-slot="scope">
                            <el-tag v-for="tag in cleanTags(scope.row.video.tags)" :key="tag" type="success">{{ tag }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="video.uploadDate" label="上传日期" width="200"></el-table-column>
                    <el-table-column prop="video.status" label="视频状态" width="100">
                        <template v-slot="scope">
                            <el-tag v-if="scope.row.video.status === 0" type="success">待审核</el-tag>
                            <el-tag v-else-if="scope.row.video.status === 1" type="warning">正常</el-tag>
                            <el-tag v-else-if="scope.row.video.status === 2" type="danger">已删除</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column fixed="right" label="操作" width="200">
                        <template v-slot="scope">
                            <el-button 
                                link
                                type="primary"
                                size="default"
                                @click="this.openEidtDialog(scope.row.video.vid)"
                            >修改状态</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="footer">
                    <el-pagination
                        @size-change="handleSizeChange"
                        @current-change="handleCurrentChange"
                        v-model:current-page="currentPage"
                        :page-sizes="[50, 75, 100]"
                        v-model:page-size="pageSize"
                        :total="1000"
                        layout="prev, pager, next, sizes"
                    >
                    </el-pagination>
                </div>

                <el-dialog
                    title="修改视频状态"
                    v-model="isEditDialogVisible"
                    align-center
                    :before-close="handleEditClose"
                    style="border-radius: 15px; padding: 24px"
                >
                    <el-form style="width: 300px;" class="custom-form">
                        <el-form-item label="视频状态">
                            <el-radio-group v-model="editStatus">
                                <el-radio value="0">待审核</el-radio>
                                <el-radio value="1">正常</el-radio>
                                <el-radio value="2">已删除</el-radio>
                            </el-radio-group>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="submitEditStatus" style="width: 60px;">确定</el-button>
                            <el-button @click="isEditDialogVisible = false" style="width: 60px;">取消</el-button>
                        </el-form-item>
                    </el-form>
                </el-dialog>
            </div>
        </div>
    </div>
</template>

<script>
import NavBar from "@/components/navbar/NavBar.vue";
import { handleTime } from '@/utils/utils';

export default {
    name: "VideoManage",
    components: {
        NavBar,
    },
    data() {
        return {
            navBarData: [
                {
                    name: "视频管理",
                }
            ],
            videoInfo: [],
            currentPage: 1,
            pageSize: 50,
            totalVideos: 0,
            isEditDialogVisible: false,
            isDelDialogVisible: false,
            editStatus: 0,
            editingVid: 0,
        }
    },
    computed: {
        paginatedVideoInfo() {
            const start = (this.currentPage - 1) * this.pageSize;
            const end = start + this.pageSize;
            return this.videoInfo.slice(start, end);
        },
    },
    methods: {
        async fetchVideoInfo() {
            const res = await this.$get("/video/get-all", {
                params: {
                    page: this.currentPage,
                    quantity: this.pageSize
                },
                headers: { Authorization: "Bearer " + localStorage.getItem("token"), },
            });

            if (res.data.code === 200) {
                this.videoInfo = res.data.data
                this.$store.commit("updateVids", this.videoInfo.map(video => video.video.vid));
            } else {
                this.$message.error(res.message);
            }
        },

        cleanTags(tags) {
            return tags.split('\r\n')
               .map(tag => tag.replace(/[^\w]/gi, ''))
               .filter(tag => tag !== '');
        },

        formatDuration(seconds) {
            return handleTime(seconds);
        },

        handleSizeChange(size) {
            this.pageSize = size;
            this.currentPage = 1;
            this.fetchVideoInfo();
        },

        handleCurrentChange(page) {
            this.currentPage = page;
            this.fetchVideoInfo();
        },

        handleEditClose(done) {
            if (done) {
                this.isEditDialogVisible = false;
                this.editingVid = null;
                this.editStatus = null;
            }
        },

        openEidtDialog(vid) {
            this.isEditDialogVisible = true;
            if (vid) {
                this.editingVid = vid;
                console.log(this.editingVid);
            }
        },

        async submitEditStatus() {

            const formData = new FormData();

            formData.append("vid", this.editingVid);
            formData.append("status", this.editStatus);

            const res = await this.$post("/video/change/status", formData, {
                headers: { Authorization: "Bearer " + localStorage.getItem("token"), },
            });

            if (res.data.code === 200) {
                this.$message.success("修改成功");
                this.fetchVideoInfo();
                this.isEditDialogVisible = false;
            } else {
                this.$message.error(res.message);
            }
        }
    },
    mounted() {
        this.fetchVideoInfo();
    }
}
</script>

<style scoped>
.category {
    color: #fff;
    line-height: 18px;
    padding: 2px 8px;
    border-radius: 10px;
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

.container-v .el-tag {
    padding: 5px;
    margin: 2px;
    
}

.footer {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 20px;
    margin-bottom: 16px;
}

.custom-form {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding-left: 16px;
    padding-right: 16px;
    padding-top: 16px;
}

.custom-form .el-form-item {
    margin-bottom: 12px;
}

.custom-form .el-radio{
    margin-left: 12px;
}

</style>