import axios from 'axios';
import store from '../store'
import { ElMessage } from 'element-plus';

export function get(url, config) {

    const instance = axios.create({
        baseURL: '/api',
        timeout: 30000,
        withCredentials: true,
    });

    instance.interceptors.request.use(
        (config) => {
            return config;
        },
        (err) => {
            console.log(err);
        },
    );

    instance.interceptors.response.use(
        (config) => {
            const code = config.data.code;
            if (code && code !== 200)
                ElMessage.error(config.data.message || '未知错误, 请打开控制台查看');
            return config;
        },
        (err) => {
            console.log(err);
            if (err.response.headers.message === 'not login') {
                // 修改当前的登录状态
                store.commit("initData");
                // 关闭websocket
                if (store.state.ws) {
                    store.state.ws.close();
                    store.commit('setWebSocket', null);
                }
                // 清除本地token缓存
                localStorage.removeItem("token");
                ElMessage.error("请登录后查看");
                store.state.isLoading = false;
            } else {
                ElMessage.error("发生未知错误");
                store.state.isLoading = false;
            }
        },
    );

    instance.defaults.withCredentials = true;

    if (config) {
        if (config.params) {
            if (config.headers) {
                return instance.get(url, {params: config.params, headers: config.headers}); // 有参数和请求头
            }
            return instance.get(url, {params: config.params}); // 有参数没请求头
        }
        if (config.headers) {
            return instance.get(url, {headers: config.headers}); // 没参数有请求头
        }
    } else {
        return instance.get(url); // 没参数和请求头
    }
}

export function post(url, data, headers) {
    const instance = axios.create({
        baseURL: '/api',
        timeout: 30000,
        withCredentials: true,
    });

    instance.interceptors.request.use(
        (config) => {
            return config;
        },
        (err) => {
            console.log(err);
        },
    );

    instance.interceptors.response.use(
        (config) => {
            const code = config.data.code;
            if (code && code !== 200)
                ElMessage.error(config.data.message || '未知错误, 请打开控制台查看');
            return config;
        },
        (err) => {
            console.log(err);
            if (err.response.headers.message == 'not login') {
                // 修改当前的登录状态
                store.commit("initData");
                // 关闭websocket
                if (store.state.ws) {
                    store.state.ws.close();
                    store.commit('setWebSocket', null);
                }
                // 清除本地token缓存
                localStorage.removeItem("token");
                ElMessage.error("请登录后查看");
                store.state.isLoading = false;
            } else {
                ElMessage.error("发生未知错误");
                store.state.isLoading = false;
            }
        },
    );

    instance.defaults.withCredentials = true;

    if (headers) {
        return instance.post(url, data, headers);
    }
    return instance.post(url, data);
}