import { createApp } from "vue";
import App from "./App.vue";
import ElementPlus from "element-plus";
import { ElMessage } from "element-plus";
import "element-plus/theme-chalk/index.css";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import axios from "axios";
import { get, post } from "./network/request";
import router from "./router";
import store from "./store";
import VueParticles from "vue3-particles";
import "vue-data-ui/style.css"

import "./assets/css/base.css";

const app = createApp(App);


app.config.globalProperties.$message = ElMessage;
app.config.globalProperties.$axios = axios;
app.config.globalProperties.$get = get;
app.config.globalProperties.$post = post;

app.use(ElementPlus, { locale: zhCn });
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.use(store);
app.use(router);
app.use(VueParticles);
app.mount("#app");
