<template>
    <div class="flex-fill">
        <div class="v-container">
            <div class="v-card" style="border-radius: 15px;">
                <NavBar :navBarItem="navBarData"></NavBar>
                <div class=main>
                    <el-table
                        :data="hotSearch"
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
                            prop="content"
                            label="关键词"
                        ></el-table-column>
                        <el-table-column
                            prop="type"
                            label="类型"
                        >
                            <template v-slot="scope">
                                <el-tag
                                    v-if="scope.row.type === 0"
                                    type="success"
                                    effect="light"
                                    size="large"
                                >
                                    普通
                                </el-tag>
                                <el-tag
                                    v-else-if="scope.row.type === 1"
                                    type="info"
                                    effect="light"
                                    size="large"
                                >
                                    新词
                                </el-tag>
                                <el-tag
                                    v-else-if="scope.row.type === 2"
                                    type="warning"
                                    effect="light"
                                    size="large"
                                >
                                    热搜
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column
                            label="操作"
                            width="200"
                        >
                            <template v-slot="scope">
                                <el-button
                                    type="parimary"
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

                <el-dialog
                    title="确认删除"
                    v-model="deleteDialogVisible"
                    width="30%"
                    align-center
                    style="border-radius: 15px; padding: 24px"
                >
                    <div style="margin-top: 16px;">
                        <p>确定要删除该热搜吗？</p>
                    </div>
                    <span class="dialog-footer">
                        <el-button @click="isDeleteDialogShow = false" style="width: 60px;">取消</el-button>
                        <el-button type="danger" @click="confirmDelete" style="width: 60px;">删除</el-button>
                    </span>
                </el-dialog>
            </div>
        </div>
    </div>
</template>

<script>
import NavBar from "@/components/navbar/NavBar.vue";

export default {
    name: "HotSearchManage",
    components: {
        NavBar
    },
    data() {
        return {
            navBarData: [
                { name: "热搜管理" },
            ],
            hotSearch:[],
            deleteDialogVisible: false,
            currentRow: null,
        };
    },
    methods: {
        async getHotSearch() {
            const res = await this.$get("/search/hot/get")
            this.hotSearch = res.data.data
            console.log(this.hotSearch)
        },

        handleDelete(row) {
            this.currentRow = row;
            this.deleteDialogVisible = true;
        },

        async confirmDelete() {

            const formData = new FormData()
            formData.append('keyword', this.currentRow.content)

            const res = await this.$post("/search/hot/delete", formData, {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token")
                }
            });

            if (res.data.code === 200) {
                this.$message({
                    message: "删除成功",
                    type: "success"
                });
                this.getHotSearch()
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
        this.getHotSearch()
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
</style>