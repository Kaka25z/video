<template>
    <div class="flex-fill">
        <div class="container-v">
            <div class="v-card" style="border-radius: 15px;">
                <NavBar :navBarItem="navBarData" @clickBarItem="clickBarItem" :style="isNavBarShow ? '' : 'display: none;'"></NavBar>
                <div class="carousel-add">
                    <el-button
                        type="primary"
                        plain
                        style="margin-top: 20px; padding: 10px 20px; margin-right: 20px; margin-left: 20px;"
                        @click="this.isaddDialogShow = true"
                    >新增轮播图</el-button>
                </div> 
                <div class="carousel-manage">
                    <el-table
                        :data="carouselInfo"
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
                        <el-table-column fixed prop="id" label="ID" />
                        <el-table-column prop="title" label="标题" />
                        <el-table-column prop="url" label="轮播图片" >
                            <template v-slot="scope">
                                <img :src="scope.row.url" alt="" style="width: 200px; height: 100px; border-radius: 10px">
                            </template>
                        </el-table-column>
                        <el-table-column prop="color" label="背景颜色">
                            <template v-slot="scope">
                                <el-tag :color="scope.row.color" style="width: 100px;" effect="dark">{{ scope.row.color }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="target" label="目标视频链接" />
                        <el-table-column fixed="right" label="操作" width="200">
                            <template v-slot="scope">
                                <el-button
                                    link
                                    type="primary"
                                    size="default"
                                    @click="this.openEidtDialog(scope.row)"
                                >编辑</el-button>
                                <el-button
                                    link
                                    type="danger"
                                    size="default"
                                    @click="this.openDeleteDialog(scope.row)"
                                >删除</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>
        </div>

        <el-dialog
            title="编辑轮播图"
            v-model="isEditDialogShow"
            :before-close="handleClose"
            style="border-radius: 15px; padding: 24px"
            align-center
        >
            <el-form 
                :model="editForm" 
                style="width: 500px; padding: 24px"
                status-icon
                label-width="80%"
                class="custom-form"
                label-position="top"
            >
                <el-form-item label="标题">
                    <el-input
                        v-model="editForm.title"
                        placeholder="请输入标题"
                        autocomplete="off"
                        clearable
                        input-style="padding-left: 10px; padding-right: 10px"
                    ></el-input>
                </el-form-item>
                <el-form-item label="轮播图片" label-width="452px">
                    <el-input
                        v-model="editForm.url"
                        placeholder="请输入图片地址"
                        autocomplete="off"
                        clearable
                        input-style="padding-left: 10px; padding-right: 10px"
                    ></el-input>
                  </el-form-item>
                <el-form-item label="背景颜色">
                    <div style="display: flex;">
                        <el-color-picker
                            v-model="editForm.color"
                            color-format="hex"
                            size="default"
                        ></el-color-picker>
                        <el-input
                            v-model="editForm.color"
                            placeholder="请输入背景颜色"
                            autocomplete="off"
                            clearable
                            style="width: 420px;"
                            input-style="padding-left: 10px; padding-right: 10px"
                        ></el-input>                        
                    </div>

                </el-form-item>
                <el-form-item label="目标视频链接">
                    <el-input
                        v-model="editForm.target"
                        placeholder="请输入视频链接"
                        autocomplete="off"
                        clearable
                        input-style="padding-left: 10px; padding-right: 10px"
                    ></el-input>
                </el-form-item>
            </el-form>
            <div class="dialog-footer">
                <el-button type="primary" @click="submitEdit" style="width: 100px;">确 定</el-button>
                <el-button @click="isEditDialogShow = false" style="width: 100px;">取 消</el-button>
            </div>
        </el-dialog>

        <el-dialog
        title="新增轮播图"
        v-model="isaddDialogShow"
        :before-close="handleClose"
        style="border-radius: 15px; padding: 24px"
        align-center
        >
            <el-form 
                :model="addForm" 
                style="width: 500px; padding: 24px"
                status-icon
                label-width="80%"
                class="custom-form"
                label-position="top"
            >
                <el-form-item label="标题">
                    <el-input
                        v-model="addForm.title"
                        placeholder="请输入标题"
                        autocomplete="off"
                        clearable
                        input-style="padding-left: 10px; padding-right: 10px"
                    ></el-input>
                </el-form-item>
                <el-form-item label="轮播图片">
                    <el-upload
                      :file-list="fileList"
                      :auto-upload="false"
                      :limit="1"
                      ref="upload"
                      :on-change="handleFileChange"
                    >
                      <el-button type="primary" plain style="width: 100px;">选择图片</el-button>
                    </el-upload>
                </el-form-item>
                <el-form-item label="背景颜色">
                    <div style="display: flex;">
                        <el-color-picker
                            v-model="addForm.color"
                            color-format="hex"
                            size="default"
                        ></el-color-picker>
                        <el-input
                            v-model="addForm.color"
                            placeholder="请输入背景颜色"
                            autocomplete="off"
                            clearable
                            style="width: 420px;"
                            input-style="padding-left: 10px; padding-right: 10px"
                        ></el-input>                        
                    </div>

                </el-form-item>
                <el-form-item label="目标视频链接">
                    <el-input
                        v-model="addForm.target"
                        placeholder="请输入视频链接"
                        autocomplete="off"
                        clearable
                        input-style="padding-left: 10px; padding-right: 10px"
                    ></el-input>
                </el-form-item>
            </el-form>
            <div class="dialog-footer">
                <el-button type="primary" @click="submitAdd" style="width: 100px;">确 定</el-button>
                <el-button @click="isaddDialogShow = false" style="width: 100px;">取 消</el-button>
            </div>
        </el-dialog>

        <el-dialog 
            v-model="isDeleteDialogShow" 
            title="确认删除"
            align-center
            style="border-radius: 15px; padding: 24px"
        >
            <div style="margin-top: 16px;">
                <p>确定要删除该轮播图吗？</p>
            </div>
            <span class="dialog-footer">
              <el-button @click="isDeleteDialogShow = false" style="width: 60px;">取消</el-button>
              <el-button type="danger" @click="confirmDelete" style="width: 60px;">删除</el-button>
            </span>
          </el-dialog>
    </div>
</template>

<script>
import NavBar from "@/components/navbar/NavBar.vue";

export default {
    name: "CarouselManage",
    components: {
        NavBar
    },
    data() {
        return {
            navBarData: [
                { id: 1, name: "轮播图管理", path: "/carouselManage" },
            ],
            isNavBarShow: true,
            carouselInfo: [],
            isEditDialogShow: false,
            isDeleteDialogShow: false,
            isaddDialogShow: false,
            editForm: {
                id: "",
                title: "",
                color: "",
                target: "",
                url: "",
            },
            addForm: {
                title: "",
                color: "",
                target: "",
                url: "",
            },
            fileList: [],
        };
    },
    methods: {
        async getCarouselInfo() {
            const res = await this.$get("/carousel/get-info");
            if (res.data.data) {
                this.carouselInfo = res.data.data;
                console.log(this.carouselInfo);
            }
        },

        openEidtDialog(row) {
            this.isEditDialogShow = true;
            console.log(row);
            this.editForm = Object.assign({}, row);
        },

        openDeleteDialog(row) {
            this.isDeleteDialogShow = true;
            this.deleteRow = Object.assign({}, row);
        },

        handleFileChange(file, fileList) {
            this.fileList = fileList.slice(-1);
        },

        async submitEdit() {
            const formData = new FormData();
            formData.append("id", this.editForm.id);
            formData.append("img", this.editForm.url);
            formData.append("title", this.editForm.title);
            formData.append("color", this.editForm.color);
            formData.append("target", this.editForm.target);


            const res = await this.$post("/carousel/update", formData, {
                headers: { Authorization: "Bearer " + localStorage.getItem("token"), },
                onUploadProgress: (progressEvent) => {
                    this.uploadProgress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                },
            });

            if (res.data.code === 200) {
                this.$message({
                    message: "编辑成功",
                    type: "success",
                });
                this.isEditDialogShow = false;
                this.getCarouselInfo();
            } else {
                this.$message({
                    message: "编辑失败",
                    type: "error",
                });
            }
        },

        async submitAdd() {
            const formData = new FormData();
            formData.append("title", this.addForm.title);
            formData.append("color", this.addForm.color);
            formData.append("target", this.addForm.target);
            formData.append('img', this.fileList[0].raw);

            const res = await this.$post("/carousel/add", formData, {
                headers: { Authorization: "Bearer " + localStorage.getItem("token"), },
                onUploadProgress: (progressEvent) => {
                    this.uploadProgress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                },
            });

            if (res.data.code === 200) {
                this.$message({
                    message: "编辑成功",
                    type: "success",
                });
                this.isaddDialogShow = false;
                this.getCarouselInfo();
            } else {
                this.$message({
                    message: "编辑失败",
                    type: "error",
                });
            }
        },

        async confirmDelete() {
            const formData = new FormData();
            formData.append("id", this.deleteRow.id);

            const res = await this.$post("/carousel/delete", formData, {
                headers: { Authorization: "Bearer " + localStorage.getItem("token") }
            });

            if (res.data.code === 200) {
                this.$message({
                    message: "删除成功",
                    type: "success",
                });
                this.isDeleteDialogShow = false;
                this.getCarouselInfo();
            } else {
                this.$message({
                    message: "删除失败",
                    type: "error",
                });
            }
        }
    },
    mounted() {
        this.getCarouselInfo();
    }
}
</script>

<style scoped>

.carousel-manage {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;

}

.carousel-add {
    display: flex;
}

.custom-form .el-form-item {
    margin-bottom: 20px;
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
</style>