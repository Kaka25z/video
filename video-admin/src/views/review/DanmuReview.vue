<template>
    <div class="flex-fill">
        <div class="v-container">
            <div class="v-card" style="border-radius: 15px;">
                <div class="danmu-table-card">
                    <div class="v-table" v-loading="loading">
                        <div class="top">
                            <div class="navbar">
                                <div class="bar-item" :class="danmuStatus === 0 ? 'active' : ''"
                                    @click="changeStatus(0)">待审核</div>
                                <div class="bar-item" :class="danmuStatus === 1 ? 'active' : ''"
                                    @click="changeStatus(1)">已通过</div>
                            </div>
                            <div class="top-right">
                                <el-input v-model="searchQuery" placeholder="搜索弹幕内容" @input="filterDanmus"
                                    clearable></el-input>
                                <div class="refresh" @click="reloadDanmus">刷新</div>
                                <div class="total">共 {{ total }} 条</div>
                            </div>
                        </div>
                        <div class="v-table__wrapper">
                            <table v-if="paginatedDanmus && paginatedDanmus.length > 0">
                                <thead>
                                    <tr>
                                        <th style="min-width: 90px;">ID</th>
                                        <th style="min-width: 200px;">弹幕内容</th>
                                        <th style="min-width: 120px;">状态</th>
                                        <th style="min-width: 160px;">操作</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="(item, index) in paginatedDanmus" :key="index">
                                        <td style="min-width: 90px;"># {{ item.id }}</td>
                                        <td style="min-width: 200px;">{{ item.content }}</td>
                                        <td style="min-width: 120px;">
                                            <div class="status" v-if="item.state === 0">
                                                <i class="iconfont icon-shenhezhong"></i>
                                                <span>待审核</span>
                                            </div>
                                            <div class="status" v-if="item.state === 1">
                                                <i class="iconfont icon-wancheng"></i>
                                                <span>已通过</span>
                                            </div>
                                        </td>
                                        <td style="min-width: 160px;">
                                            <el-button type="primary" size="small"
                                                @click="openStatusDialog(item)">修改状态</el-button>
                                            <el-button type="danger" size="small"
                                                @click="deleteDanmu(item.id)">删除</el-button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                            <div class="no-more" v-if="!loading && paginatedDanmus && paginatedDanmus.length == 0">
                                <img src="~assets/img/silly.png" alt="">
                                <span>暂无数据</span>
                            </div>
                        </div>
                        <div class="v-table-page">
                            <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize"
                                :pager-count="pagerCount" :current-page="page"
                                @current-change="pageChange"></el-pagination>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <el-dialog v-model="statusDialogVisible" title="修改状态" style="border-radius: 15px; padding: 24px" align-center>
            <el-form :model="currentDanmu" class="custom-form">
                <el-form-item label="状态">
                    <el-select v-model="currentDanmu.state" placeholder="请选择状态" style="width: 160px; height: 40px; margin-left: 20px;">
                        <el-option label="待审核" :value="2"></el-option>
                        <el-option label="已通过" :value="1"></el-option>
                    </el-select>
                </el-form-item>
            </el-form>
            <template v-slot:footer>
                <el-button @click="statusDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="updateStatus">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script>
export default {
    name: "DanmuReview",
    data() {
        return {
            danmuStatus: 0, // 要查询的弹幕状态，0 待审核，1 已通过
            danmus: [], // 初始化为空数组
            filteredDanmus: [], // 过滤后的弹幕数据
            paginatedDanmus: [], // 当前页显示的弹幕
            searchQuery: '', // 搜索查询
            page: 1,
            pageSize: 10, // 每页显示的数量
            total: 0, // 初始化总数为0
            pagerCount: 7,
            loading: true,
            statusDialogVisible: false, // 状态对话框可见性
            currentDanmu: {}, // 当前选择的弹幕
        };
    },
    methods: {
        // 请求
        // 查询弹幕数量
        async getTotal() {
            const res = await this.$get(this.danmuStatus === 0 ? '/danmu/pending' : '/danmu/approved', {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token"),
                },
            });
            if (res.data.data) {
                this.danmus = res.data.data; // 设置弹幕数据
                this.filteredDanmus = this.danmus; // 初始化过滤后的弹幕数据
                this.total = this.danmus.length; // 设置总数
                this.paginateDanmus(); // 切分当前页数据
            } else {
                this.total = 0;
                this.danmus = [];
                this.filteredDanmus = [];
                this.paginatedDanmus = [];
            }
        },

        // 切分当前页数据
        paginateDanmus() {
            const start = (this.page - 1) * this.pageSize;
            const end = start + this.pageSize;
            this.paginatedDanmus = this.filteredDanmus.slice(start, end);
        },

        // 事件
        // 切换类型
        changeStatus(status) {
            this.danmuStatus = status;
            if (this.page !== 1) {
                this.page = 1;  // 这里页码改变会触发重加载
            } else {
                this.reloadDanmus();
            }
        },

        // 改变页码时的回调
        async pageChange(page) {
            this.page = page;
            this.paginateDanmus(); // 切分当前页数据
        },

        // 重新加载弹幕列表
        async reloadDanmus() {
            this.loading = true;
            await this.getTotal();
            this.loading = false;
        },

        // 打开状态对话框
        openStatusDialog(danmu) {
            this.currentDanmu = { ...danmu };
            this.statusDialogVisible = true;
        },

        // 更新状态
        async updateStatus() {
            const res = await this.$post(`/danmu/approve/${this.currentDanmu.id}`, { status: this.currentDanmu.status }, {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token"),
                },
            });
            if (res.data.code === 200) {
                this.$message.success('状态修改成功');
                this.reloadDanmus();
                this.statusDialogVisible = false;
            } else {
                this.$message.error(res.message);
            }
        },

        // 删除弹幕
        async deleteDanmu(id) {
            const res = await this.$post(`/danmu/delete`, { id }, {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token"),
                },
            });
            if (res.data.code === 200) {
                this.$message.success('删除成功');
                this.reloadDanmus();
            } else {
                this.$message.error(res.message);
            }
        },

        // 过滤弹幕数据
        filterDanmus() {
            if (this.searchQuery) {
                this.filteredDanmus = this.danmus.filter(danmu => danmu.content.includes(this.searchQuery));
            } else {
                this.filteredDanmus = this.danmus;
            }
            this.total = this.filteredDanmus.length;
            this.paginateDanmus();
        },
    },
    async created() {
        await this.getTotal();
        this.loading = false;
    },
    mounted() {
        // 监听窗口大小变化，判断是否小窗
        window.addEventListener('resize', this.changeWidth);
    },
    unmounted() {
        window.removeEventListener('resize', this.changeWidth);
    }
};
</script>

<style scoped>
.danmu-table-card {
    height: calc(100vh - 96px);
    position: relative;
    overflow: hidden !important;
    overflow-anchor: none;
    -ms-overflow-style: none;
    touch-action: auto;
    -ms-touch-action: auto;
}

.v-table {
    --v-table-row-height: 120px;
}

.top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 64px;
    border-bottom: 1px solid #e7e7e7;
}

.navbar,
.top-right {
    display: flex;
    flex: 0 0 auto;
}

.top-right {
    margin-left: 100px;
    align-items: center;
}

.el-input {
    width: 300px;
    margin-right: 20px;
}

.el-input__inner {
    padding: 10px;
}

.refresh {
    cursor: pointer;
    color: var(--brand_blue);
    margin-right: 20px;
}

.refresh:hover {
    color: var(--Lb6);
}

.total {
    font-size: 16px;
    color: #505050;
}

.bar-item {
    flex: 0 0 auto;
    height: 64px;
    padding-bottom: 18px;
    padding-top: 26px;
    margin-left: 40px;
    font-size: 16px;
    color: #505050;
    cursor: pointer;
}

.bar-item.active {
    color: var(--brand_pink);
    font-weight: 600;
    border-bottom: 3px solid var(--brand_pink);
}

.v-table__wrapper {
    height: calc(100% - 150px);
}

.v-table__wrapper table {
    padding: 0 4px 8px;
}

.cover {
    height: 81px;
    width: 144px;
    object-fit: cover;
    box-shadow: 2px 2px 8px #0000001f;
}

.nickname {
    cursor: pointer;
}

.nickname:hover {
    color: var(--text1);
}

.category {
    color: #fff;
    line-height: 18px;
    padding: 2px 8px;
    border-radius: 10px;
}

.no-more {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 150px;
    width: 100%;
}

.no-more img {
    height: 80px;
}

.no-more span {
    font-size: 20px;
    color: var(--text3);
    line-height: 40px;
}

.v-table-page {
    width: 100%;
    display: flex;
    justify-content: center;
}

.custom-form{
    margin-bottom: 20px;
    margin-top: 20px;
    display: flex;
    justify-content: center
}
</style>