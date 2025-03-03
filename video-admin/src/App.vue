<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script>
import { ElLoading } from 'element-plus';

export default {
  name: "App",
  components: {},
  data() {
    return {
      // 加载蒙版的显隐过度
      maskDisplay: "none",
      isMaskShow: false,
      loadingInstance: null,
    }
  },
  methods: {
    // 获取频道列表
    async getChannels() {
      let res = await this.$get("/category/getall");
      // console.log("频道列表: ", res);
      this.$store.commit("updateChannels", res.data.data);
    },

    // 加载蒙版的延迟渲染效果
    show() {
      this.maskDisplay = "";
      this.isMaskShow = true;
    },
    hide() {
      this.isMaskShow = false;
      setTimeout(() => {
        this.maskDisplay = "none";
      }, 200);
    },
    showLoading() {
        if (!this.loadingInstance) {
            this.loadingInstance = ElLoading.service({
            lock: true,
            text: '加载中...',
            background: 'rgba(0, 0, 0, 0.7)',
            });
        }
      },
      hideLoading() {
        if (this.loadingInstance) {
            this.loadingInstance.close();
            this.loadingInstance = null;
        }
      },
  },
  created() {
    // 如果缓存中有token，尝试获取用户数据
    if (localStorage.getItem("token")) {
      this.$store.dispatch("getPersonalInfo");
    }
    this.getChannels();
  },
  mounted() {
    document.title = "BiliMini-admin";
  },
  watch: {
      "$store.state.isLoading"(current) {
          if (current) {
              this.show();
              this.showLoading();
          } else {
              this.hide();
              this.hideLoading();
          }
      },
  }
};
</script>

<style>
#app {
    margin: 0 auto;
    background-color: var(--bg1);
}

.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 50000;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.loading-box {
  display: flex;
  height: 100vh;
  width: 100vw;
  align-items: center;
  justify-content: center;
}

.loading-box img {
  max-height: 33vh;
  max-width: 33vw;
}

.hide {
  animation: fade-out 0.2s ease-out forwards;
}

.show {
  animation: fade-in 0.2s ease-out forwards;
}

/* 淡入动画 */
@keyframes fade-in {
    0% {
        opacity: 0; /* 初始状态透明 */
    }
    100% {
        opacity: 1; /* 最终状态不透明 */
    }
}

/* 淡出动画 */
@keyframes fade-out {
    0% {
        opacity: 1; /* 初始状态不透明 */
    }
    100% {
        opacity: 0; /* 最终状态透明 */
    }
}
</style>
