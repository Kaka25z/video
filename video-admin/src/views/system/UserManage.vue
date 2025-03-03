<template>
    <div class="main">
        <el-table
          :data="userInfo"
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
          <el-table-column fixed prop="uid" label="UID" />
          <el-table-column prop="username" label="用户账号" />
          <el-table-column prop="nickname" label="用户昵称" />
          <el-table-column prop="avatar" label="用户头像">
            <template v-slot="scope">
              <el-image
                fit="cover"
                :preview-teleported="true"
                :src="scope.row.avatar"
                :preview-src-list="[scope.row.avatar]"
                class="img-avatar"
              />
            </template>
          </el-table-column>
          <el-table-column prop="role" label="用户身份">
            <template v-slot="scope">
              <el-tag size="large">{{ getRoleLabel(scope.row.role) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="state" label="用户状态">
            <template v-slot="scope">
                <el-tag size="large">{{ getStateLable(scope.row.state) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auth" label="用户认证">
            <template v-slot="scope">
                <el-tag size="large">{{ getAuthLable(scope.row.auth) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="authMsg" label="认证信息" >
            <template v-slot="scope">
                {{ !scope.row.authMsg ? '无' : scope.row.authMsg }}
            </template>
          </el-table-column>
          <el-table-column prop="createDate" label="创建时间" />
          <el-table-column fixed="right" label="选项">
            <template v-slot="scope">
              <el-button
                link
                type="primary"
                size="default"
                @click="this.openDialog(scope.row)"
              >
                修改信息
              </el-button>
                <el-button
                link
                type="primary"
                size="default"
                @click="this.openPasswdDialog(scope.row)"
                >
                更新密码
                </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-dialog
            v-model="dialogVisible"
            title="修改用户"
            :before-close="handleClose"
            style="border-radius: 15px; padding: 24px"
            align-center
        >
            <el-form
                ref="ruleFormRef"
                style="width: 500px; padding: 24px"
                status-icon
                :model="ruleForm"
                :rules="formRules"
                label-width="80%"
                class="custom-form"
                label-position="top"
            >
                <el-form-item label="用户名" prop="username">
                    <el-input
                        v-model="ruleForm.nickname"
                        type="text"
                        autocomplete="off"
                        clearable
                        input-style="padding-left: 10px; padding-right: 10px"
                    />
                </el-form-item>
                <el-form-item label="用户身份" prop="role">
                    <el-select
                        v-model="ruleForm.role"
                        placeholder="请选择用户身份"
                        clearable
                    >
                        <el-option
                            v-for="item in role"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="用户状态" prop="state">
                    <el-select
                        v-model="ruleForm.state"
                        placeholder="请选择用户状态"
                        clearable
                    >
                        <el-option
                            v-for="item in state"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="用户认证" prop="auth">
                    <el-select
                        v-model="ruleForm.auth"
                        placeholder="请选择用户认证"
                        clearable
                    >
                        <el-option
                            v-for="item in auth"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="认证信息" prop="authMsg">
                    <el-input
                        v-model="ruleForm.authMsg"
                        type="text"
                        autocomplete="off"
                        clearable
                        input-style="padding-left: 10px; padding-right: 10px"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button
                        style="margin-top: 10px; width: 70px"
                        type="primary"
                        @click="updateUserProfile()"
                    >
                        确认
                    </el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog
            v-model="passwdDialogVisible"
            title="更新密码"
            :before-close="handleClose"
            style="border-radius: 15px; padding: 24px"
            align-center
        >
            <el-form
                style="width: 500px; padding: 24px"
                status-icon
                :model="passwordForm"
                :rules="passowrdRules"
                label-width="80%"
                class="custom-form"
                label-position="top"
            >
                <el-form-item prop="newPassword">
                    <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    autocomplete="off"
                    clearable
                    input-style="padding-left: 10px; padding-right: 10px"
                    placeholder="请输入密码"
                    >  
                    </el-input>
                </el-form-item>
                <el-form-item prop="confirmPassword">
                    <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    autocomplete="off"
                    clearable
                    input-style="padding-left: 10px; padding-right: 10px"
                    placeholder="请再次输入密码"
                    >
                    </el-input>
                </el-form-item>
                <el-form-item>
                    <el-button
                        style="margin-top: 10px; width: 70px"
                        type="primary"
                        @click="updateUserPassword()"
                    >
                        更新密码
                    </el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
      </div>
</template>

<script>
export default {
    name: "UserManage",
    data() {
        return {
            ruleForm: {
                uid: null,
                nickname: '',
                role: null,
                state: null,
                auth: null,
                authMsg: '',
            },
            passwordForm: {
                uid: null,
                newPassword: "",
                confirmPassword: "",
            },
            userInfo: [],
            dialogVisible: false,
            passwdDialogVisible: false,
            role: [
                {
                    value: 0,
                    label: "普通用户",
                },
                {
                    value: 1,
                    label: "管理员",
                },
                {
                    value: 2,
                    label: "超级管理员",
                },
            ],
            state: [
                {
                    value: 0,
                    label: "正常",
                },
                {
                    value: 1,
                    label: "封禁",
                },
                {
                    value: 2,
                    label: "注销",
                },
            ],
            auth: [
                {
                    value: 0,
                    label: "普通用户",
                },
                {
                    value: 1,
                    label: "个人认证",
                },
                {
                    value: 2,
                    label: "机构认证",
                },
            ],
            formRules: {
                username: [
                    { required: true, message: "请输入用户名", trigger: "blur" },
                ],
                password: [
                    { required: true, message: "请输入密码", trigger: "blur" },
                ],
                role: [
                    { required: true, message: "请选择用户身份", trigger: "blur" },
                ],
                state: [
                    { required: true, message: "请选择用户状态", trigger: "blur" },
                ],
            },
            passowrdRules: {
                newPassword: [
                    { required: true, message: "请输入密码", trigger: "blur" },
                ],
                confirmPassword: [
                    { required: true, message: "请再次输入密码", trigger: "blur" },
                ],
            },
        }
    },
    methods: {
        async getUserInfo() {
            const res = await this.$get("/user/account/get-all", {
                headers: {
                    Authorization: "Bearer " + localStorage.getItem("token"),
                },
            });
            this.userInfo = res.data.data;
            this.userInfo.forEach((item) => {
                item.createDate = this.formatDate(item.createDate);
            });
            console.log(this.userInfo);

        },

        formatDate(dateString) {
            const date = new Date(dateString);
            return date.toLocaleString('zh-CN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
        },
        
        getRoleLabel(role) {
            switch (role) {
                case 0:
                return "普通用户";
                case 1:
                return "管理员";
                case 2:
                return "超级管理员";
                default:
                return "未知身份";
            }
        },

        getStateLable(state) {
            switch (state) {
                case 0:
                return "正常";
                case 1:
                return "封禁";
                case 2:
                return "注销";
                default:
                return "未知状态";
            }
        },

        getAuthLable(auth) {
            switch (auth) {
                case 0:
                return "普通用户";
                case 1:
                return "个人认证";
                case 2:
                return "机构认证";
            }
        },

        openDialog(rowData) {
            this.dialogVisible = true;
            this.ruleForm = Object.assign({}, rowData);
        },

        openPasswdDialog(rowData) {
            this.passwdDialogVisible = true;
            this.passwordForm.uid = rowData.uid;
        },

        async updateUserProfile() {
            try {

                const formData = new FormData();

                formData.append("uid", this.ruleForm.uid);
                formData.append("nickname", this.ruleForm.nickname);
                formData.append("role", this.ruleForm.role);
                formData.append("state", this.ruleForm.state);
                formData.append("auth", this.ruleForm.auth);
                formData.append("authMsg", this.ruleForm.authMsg);

                const res = await this.$post("/admin/account/update-user-profile", formData, {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("token"),
                    }
                });
                if (res.data.code === 200) {
                    this.$message({
                        message: "修改成功",
                        type: "success",
                    });
                    this.dialogVisible = false;
                    this.getUserInfo();
                } else {
                    this.$message({
                        message: "服务器内部错误",
                        type: "error",
                    });
                }
            } catch (error) {
                this.$message({
                    message: "服务器内部错误",
                    type: "error",
                });
            }
        },

        async updateUserPassword() {
            try {
                if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
                    this.$message({
                        message: "两次输入的密码不一致",
                        type: "error",
                    });
                    return;
                }

                const formData = new FormData();

                formData.append("uid", this.passwordForm.uid);
                formData.append("npw", this.passwordForm.newPassword);

                const res = await this.$post("/admin/account/update-password", formData, {
                    headers: {
                        Authorization: "Bearer " + localStorage.getItem("token"),
                    }
                });
                if (res.data.code === 200) {
                    this.$message({
                        message: "修改成功",
                        type: "success",
                    });
                    this.passwdDialogVisible = false;
                    this.getUserInfo();
                } else {
                    this.$message({
                        message: "服务器内部错误",
                        type: "error",
                    });
                }
            } catch (error) {
                this.$message({
                    message: "服务器内部错误",
                    type: "error",
                });
            }
        },

    },
    mounted() {
        this.getUserInfo();
    }
}
</script>

<style scoped>
.img-avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    vertical-align: middle;
}

.main {
    padding: 40px;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}

.custom-form .el-form-item {
    margin-bottom: 20px;
}
</style>