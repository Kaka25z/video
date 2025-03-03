<template>
    <div class="flex-fill">
        <div class="container-v">
            <div class="v-card" style="border-radius: 15px;">
                <NavBar :navBarItem="navBarData"></NavBar>
                <el-table
                    :data="paginatedData"
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
                    <el-table-column fixed prop="mcName" label="分区名称" width="100" />
                    <el-table-column label="子分区">
                        <template v-slot="scope">
                            <el-table
                                :data="scope.row.scList"
                                border
                                style="
                                width: auto;
                                z-index: 0;
                                margin-top: 16px;
                                margin-bottom: 8px;
                                "
                                stripe
                                table-layout="auto"
                                size="small"
                            >
                                <el-table-column prop="scName" label="子分区名称" />
                                <el-table-column prop="rcmTag" label="分区标签">
                                    <template v-slot="scope">
                                        <div class="tag-container">
                                            <el-tag
                                                v-for="tag in scope.row.rcmTag"
                                                :key="tag"
                                                class="tag-item tag-with-padding"
                                                :type="getRandomType()"
                                                :effect="getRandomEffect()"
                                                size="large"
                                            >
                                                {{ tag }}
                                            </el-tag>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column fixed="right" label="操作">
                                    <template v-slot=scope>
                                        <el-button
                                            link
                                            type="primary"
                                            size="default"
                                            class="button"
                                            @click="this.openEidtDialog(scope.row.scId)"
                                        >添加标签</el-button>
                                        <el-button
                                            link
                                            type="danger"
                                            size="default"
                                            class="button"
                                            @click="this.openDeleteDialog(scope.row.scId)"
                                        >删除标签</el-button>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </template>
                    </el-table-column>
                </el-table>
                <el-pagination
                    class="page-footer"
                    layout="prev, pager, next"
                    background
                    :total="tagInfo.length"
                    :page-size="2"
                    @current-change="handlePageChange"
                ></el-pagination>

                <el-dialog
                    title="添加标签"
                    v-model="dialogVisible"
                    style="border-radius: 15px; padding: 24px"
                    align-center
                >
                    <el-form :model="editForm" class="custom-form">
                        <el-form-item label="标签名称">
                            <el-input
                                v-model="editForm.tagName"
                                placeholder="请输入标签名称"
                                autocomplete="off"
                                input-style="padding-left: 10px; padding-right: 10px"
                                style="padding-left: 10px;"
                            ></el-input>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="addTag" style="width: 60px; margin-left: 20px">添加</el-button>
                        </el-form-item>
                    </el-form>
                </el-dialog>

                <el-dialog
                    title="删除标签"
                    v-model="isDeleteDialogVisible"
                    style="border-radius: 15px; padding: 24px"
                    align-center
                >
                    <el-form :model="deleteForm" class="custom-form">
                        <el-form-item label="标签名称">
                            <el-input
                                v-model="deleteForm.tagName"
                                placeholder="请输入要删除的标签名称"
                                autocomplete="off"
                                input-style="padding-left: 10px; padding-right: 10px"
                                style="padding-left: 10px;"
                            ></el-input>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="delTag" style="width: 60px; margin-left: 20px">删除</el-button>
                        </el-form-item>
                    </el-form>
                </el-dialog>
            </div>
        </div>
    </div>
</template>

<script>
import NavBar from "@/components/navbar/NavBar.vue";

export default {
    name: "TagManage",
    components: {
        NavBar
    },
    data() {
        return {
            navBarData:[
                {name: "标签管理"},
            ],
            tagInfo: [],
            currentPage: 1,
            dialogVisible: false,
            editForm: {
                mcId: null,
                scId: null,
                tagName: ''
            },
            isDeleteDialogVisible: false,
            deleteForm: {
                mcId: null,
                scId: null,
                tagName: ''
            },
            colors: ['primary', 'success', 'warning', 'danger', 'info'],
            effect: ['light', 'plain']
        }
    },
    computed: {
        paginatedData() {
            const start = (this.currentPage - 1) * 2;
            const end = start + 2;
            return this.tagInfo.slice(start, end);
        }
    },
    methods: {
        async getTagInfo() {
            const res = await this.$get("/category/getall")
            if (!res.data.data) return;
            this.tagInfo = this.processTagInfo(res.data.data);
            console.log(this.tagInfo)
        },

        getRandomType() {
            const randomIndex = Math.floor(Math.random() * this.colors.length);
            return this.colors[randomIndex];
        },

        getRandomEffect() {
            const randomIndex = Math.floor(Math.random() * this.effect.length);
            return this.effect[randomIndex];
        },

        processTagInfo(data) {
            const mergedData = {};

            data.forEach(item => {
                if (!mergedData[item.mcId]) {
                    mergedData[item.mcId] = {
                        mcId: item.mcId,
                        mcName: item.mcName,
                        scList: []
                    };
                }

                item.scList.forEach(scItem => {
                    const existingScItem = mergedData[item.mcId].scList.find(sc => sc.scId === scItem.scId);
                    if (!existingScItem) {
                        mergedData[item.mcId].scList.push({
                            scId: scItem.scId,
                            scName: scItem.scName,
                            rcmTag: new Set(scItem.rcmTag)
                        });
                    } else {
                        scItem.rcmTag.forEach(tag => existingScItem.rcmTag.add(tag));
                    }
                });
            });

            // Convert Set to Array
            Object.keys(mergedData).forEach(key => {
                mergedData[key].scList.forEach(scItem => {
                    scItem.rcmTag = Array.from(scItem.rcmTag);
                });
            });

            return Object.values(mergedData);
        },

        handlePageChange(page) {
            this.currentPage = page;
        },

        findMcId(scId) {
            let mcId = null;
            Object.keys(this.tagInfo).forEach(key => {
                const mcItem = this.tagInfo[key];
                mcItem.scList.forEach(scItem => {
                    if (scItem.scId === scId) {
                        mcId = mcItem.mcId;
                    }
                });
            });

            return mcId;
        },

        openEidtDialog(scId) {
            const mcId = this.findMcId(scId);
            if (mcId) {
                this.editForm = {
                    mcId,
                    scId,
                    tagName: ''
                };
                this.dialogVisible = true;
            }
        },

        openDeleteDialog(scId) {
            const mcId = this.findMcId(scId);
            if (mcId) {
                this.deleteForm = {
                    mcId,
                    scId,
                    tagName: ''
                };
                this.isDeleteDialogVisible = true;
            }
        },


        async addTag() {
            const { mcId, scId, tagName } = this.editForm;
            if (!tagName) {
                this.$message.error('标签名称不能为空');
                return;
            }

            const formData = new FormData();
            formData.append('mcId', mcId);
            formData.append('scId', scId);
            formData.append('rcmTag', tagName);

            const res = await this.$post('/category/addTag', formData, {
                headers: { Authorization: "Bearer " + localStorage.getItem("token"), }
            });

            if (res.data.code === 200) {
                this.$message.success('添加成功');
                this.getTagInfo();
                this.dialogVisible = false;
            } else {
                this.$message.error('添加失败');
            }
        },

        async delTag() {
            const { mcId, scId, tagName } = this.deleteForm;
            if (!tagName) {
                this.$message.error('标签名称不能为空');
                return;
            }

            const formData = new FormData();
            formData.append('mcId', mcId);
            formData.append('scId', scId);
            formData.append('rcmTag', tagName);

            const res = await this.$post('/category/removeTag', formData, {
                headers: { Authorization: "Bearer " + localStorage.getItem("token"), }
            });

            if (res.data.code === 200) {
                this.$message.success('删除成功');
                this.getTagInfo();
                this.isDeleteDialogVisible = false;
            } else {
                this.$message.error('删除失败');
            }
        },
    },
    
    mounted() {
        this.getTagInfo();
    }
}
</script>

<style scoped>
.tag-container {
    width: 80%;
    max-height: 200px; 
    overflow-y: auto;
    padding: 5px;
    box-sizing: border-box;
}

.tag-item {
    margin-right: 5px;
    margin-bottom: 5px;
}

.tag-with-padding {
    padding-left: 12px;
    padding-right: 12px;
    font-size: 14px;
}

.custom-form{
    margin-bottom: 20px;
    margin-top: 20px;
    display: flex;
    justify-content: center
}

.page-footer {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    margin-bottom: 26px;
}

.button {
    padding-left: 5px;
    padding-right: 5px;
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