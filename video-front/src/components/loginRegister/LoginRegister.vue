<template>
    <div class="login-register">
        <div class="login-register-container">
            <el-tabs stretch class="login-tabs" @tab-click="handleClick">
                <el-tab-pane label="登录" lazy>
                    <div class="login-box">
                        <el-input type="text" class="input" v-model="usernameLogin" placeholder="请输入账号" />
                        <el-input type="password" show-password class="input" v-model="passwordLogin"
                            placeholder="请输入密码" />
                        <div class="verify">
                            <el-input type="text" class="input" v-model="verifyCode.code" placeholder="请输入验证码" />
                            <img :src="verifyCode.image" alt="验证码" @click="getVerifyCode" />
                        </div>
                        <div class="submit" @click="submitLogin">登&nbsp;录</div>
                        <div class="tips">
                            登录即代表你同意我们的<span class="agreement">用户协议</span>
                        </div>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="注册" lazy>
                    <div class="register-box">
                        <el-input type="text" class="input" v-model="usernameRegister" placeholder="请输入账号"
                            maxlength="50" />
                        <el-input type="password" show-password class="input" v-model="passwordRegister"
                            placeholder="请输入密码" />
                        <el-input type="password" show-password class="input" v-model="confirmedPassword"
                            placeholder="再次确认密码" />
                            <div class="verify">
                                <el-input type="text" class="input" v-model="verifyCode.code" placeholder="请输入验证码" />
                                <img :src="verifyCode.image" alt="验证码" @click="getVerifyCode" />
                            </div>
                        <div class="submit" @click="submitRegister">注&nbsp;册</div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<script>
import { ElMessage } from "element-plus";
import axios from "axios";

export default {
    name: "LoginRegister",
    data() {
        return {
            videoElement: null,
            usernameLogin: "",
            passwordLogin: "",
            usernameRegister: "",
            passwordRegister: "",
            confirmedPassword: "",
            type: 1,
            verifyCode: {
                key: "",
                code: "",
                image: "",
            },
        };
    },
    mounted() {
        this.videoElement = this.$refs.loginVideo;
        document.addEventListener("keydown", (e) => this.handleKeyboard(e));
        this.getVerifyCode();
    },
    beforeUnmount() {
        document.removeEventListener("keydown", (e) => this.handleKeyboard(e));
    },
    methods: {
        // 点击标签页触发的事件
        handleClick(tab) {
            if (tab.props.label === "登录") {
                this.type = 1;
            } else {
                this.type = 2;
            }
        },

        // 监听键盘回车触发登录
        handleKeyboard(event) {
            if (event.keyCode === 13 && this.type === 1) {
                this.submitLogin();
            }
        },

        async getVerifyCode() {
            const res = await this.$get("/captcha/get");

            if (!res.data) return;
            this.verifyCode = res.data.data;
            console.log("验证码: ", this.verifyCode);
        },

        async VerifyCode() {

            const formData = new FormData();

            formData.append("key", this.verifyCode.key);
            formData.append("code", this.verifyCode.code);

            const res = await this.$post("/captcha/verify", formData);

            if (!res.data) return;

            return res;
        },

        // 登录的回调
        async submitLogin() {
            // 前端先做判断，减轻服务器负担
            if (this.usernameLogin.trim() == "") {
                ElMessage.error("请输入账号");
                return;
            }
            if (this.passwordLogin == "") {
                ElMessage.error("请输入密码");
                return;
            }

            if (this.verifyCode.code == "") {
                ElMessage.error("请输入验证码");
                return;
            }
            this.$store.state.isLoading = true;

            const verify = await this.VerifyCode();

            if (verify.data.code !== 200) {
                ElMessage.error(verify.data.message);
                this.$store.state.isLoading = false;
                return;
            }

            const result = await axios.post("/api/user/account/login", {
                username: this.usernameLogin.toString(),
                password: this.passwordLogin.toString(),
            }).catch(() => {
                ElMessage.error("未知错误");
                this.$store.state.isLoading = false;
            });
            if (!result) {
                this.$store.state.isLoading = false;
                return;
            }
            if (result.data.code !== 200) {
                ElMessage.error(result.data.message);
                this.$store.state.isLoading = false;
            }
            if (result.data.code === 200) {
                localStorage.setItem("token", result.data.data.token);
                this.$store.commit("updateUser", result.data.data.user);
                await this.$store.dispatch("getMsgUnread");
                await this.initIMServer();
                await this.getFavorites();
                await this.getLikeAndDisLikeComment();
                ElMessage.success(result.data.message);
                this.$store.commit("updateIsLogin", true);
                this.$emit("loginSuccess");
                this.$store.state.isLoading = false;
                location.reload();
            }
        },

        async submitRegister() {
            // 前端先做判断，减轻服务器负担
            if (this.usernameRegister.trim() == "") {
                ElMessage.error("账号不能为空");
                return;
            }
            if (this.passwordRegister == "" || this.confirmedPassword == "") {
                ElMessage.error("密码不能为空");
                return;
            }
            if (this.passwordRegister != this.confirmedPassword) {
                ElMessage.error("两次输入的密码不一致");
                return;
            }
            if (this.verifyCode.code == "") {
                ElMessage.error("请输入验证码");
                return;
            }

            const verify = await this.VerifyCode();

            if (verify.data.code !== 200) {
                ElMessage.error(verify.data.message);
                return;
            }


            const result = await this.$post("/user/account/register", {
                username: this.usernameRegister.toString(),
                password: this.passwordRegister.toString(),
                confirmedPassword: this.confirmedPassword.toString(),
            });
            if (!result) return;
            if (result.data.code === 200) {
                ElMessage.success(result.data.message);
                this.usernameRegister = "";
                this.passwordRegister = "";
                this.confirmedPassword = "";
                this.verifyCode.code == "";
                this.getVerifyCode();
                this.handleClick({ props: { label: "登录" } });
            }
        },

        // 开启实时通信消息服务
        async initIMServer() {
            await this.$store.dispatch("connectWebSocket");
            const connection = JSON.stringify({
                code: 100,
                content: "Bearer " + localStorage.getItem("token"),
            });
            this.$store.state.ws.send(connection);
        },

        // 获取当前用户的收藏夹列表
        async getFavorites() {
            const res = await this.$get("/favorite/get-all/user", {
                params: { uid: this.$store.state.user.uid },
                headers: { Authorization: "Bearer " + localStorage.getItem("token") },
            });
            if (!res.data.data) return;
            // 将默认置顶
            const defaultFav = res.data.data.find((item) => item.type === 1);
            const list = res.data.data.filter((item) => item.type !== 1);
            list.unshift(defaultFav);
            this.$store.commit("updateFavorites", list);
        },

        

        // 获取用户赞踩的评论集合
        async getLikeAndDisLikeComment() {
            const res = await this.$get("/comment/get-like-and-dislike", {
                params: { uid: this.$store.state.user.uid },
                headers: { Authorization: "Bearer " + localStorage.getItem("token") },
            });
            if (!res.data) return;
            this.$store.commit("updateLikeComment", res.data.data.userLike);
            this.$store.commit("updateDislikeComment", res.data.data.userDislike);
        },
        async getChannels() {
            let res = await this.$get("/category/getall");
            console.log("频道列表: ", res);
            this.$store.commit("updateChannels", res.data.data);
        },

        async getHotSearch() {
            const res = await this.$get("/search/hot/get");
            this.$store.commit("updateTrendings", res.data.data);
        },
    },
};
</script>

<style scoped>
.login-register {
    position: relative;
    display: flex;
    width: 100%;
    height: 100%;
}

.canvas-wrapper {
    position: relative;
    width: 360px;
    height: 360px;
}

.video-wrapper {
    visibility: hidden;
    position: absolute;
    width: 360px;
    height: 360px;
}

.video-wrapper video {
    object-fit: fill;
    display: block;
}

#cvs {
    visibility: hidden;
    position: absolute;
}

#cvs2 {
    position: absolute;
    top: 4px;
    left: 5px;
}

.login-register-container {
    display: block;
    width: 460px;
    height: 420px;
    padding: 30px 40px;
}

.login-tabs {
    width: 80%;
    margin: 0 auto;
}

.login-box,
.register-box {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.login-box .input,
.login-box .submit,
.login-box .tips {
    margin-top: 30px;
    width: 100%;
}

.register-box .input,
.register-box .submit,
.register-box .tips {
    margin-top: 20px;
    width: 100%;
}

.submit {
    color: #fff;
    border-radius: 4px;
    background-color: var(--brand_pink);
    text-align: center;
    padding: 10px 15px;
    cursor: pointer;
}

.submit:hover {
    background-color: #f992af;
}

.tips {
    color: var(--text2);
    font-size: 12px;
    text-align: center;
}

.tips .agreement {
    color: var(--brand_blue);
    margin-left: 4px;
    cursor: pointer;
}

/* element 元素 */
.el-input {
    --el-input-focus-border: #ccc;
    --el-input-focus-border-color: #ccc;
    --el-input-border-radius: 10px;
    --el-input-height: 40px;
}

.el-input /deep/ .el-input__inner {
    padding: 8px 15px;
}

.el-input /deep/ .el-input__icon {
    margin-right: 8px;
}

.login-register-container /deep/ .el-tabs__active-bar {
    height: 3px;
}

.login-register-container /deep/ .el-tabs__nav-wrap::after {
    height: 0;
}

.login-register-container /deep/ .el-tabs__item {
    font-size: 17px;
}

.verify {
    display: flex;
    align-items: center;
    justify-content: center;
}

.verify img {
    width: 100px;
    height: 38px;
    margin-left: 10px;
    margin-top: 30px;
    cursor: pointer;
}
</style>
