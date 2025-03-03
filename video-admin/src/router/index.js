import { createRouter, createWebHistory } from "vue-router";

const Login = () => import("@/views/LoginVue.vue");
const Index = () => import("@/views/IndexVue.vue");
const NotFound = () => import("@/views/NotFound.vue");
const Home = () => import("@/views/homePage/HomePage.vue");
const Data = () => import("@/views/data/DataCenter.vue");
const Carousel = () => import("@/views/content/CarouselManage.vue");
const HotSearch = () => import("@/views/content/HotSearchManage.vue");
const Video = () => import("@/views/review/VideoReview.vue");
const VideoDetail = () => import("@/views/review/detail/VideoDetail.vue");
const User = () => import("@/views/system/UserManage.vue");
const Tag = () => import("@/views/content/TagManage.vue");
const VideoManage = () => import("@/views/content/VideoManage.vue");
const CommentManage = () => import("@/views/content/CommentManage.vue");
const DanmuReview = () => import("@/views/review/DanmuReview.vue");
const SystemInfo =() => import('@/views/system/SystemInfo.vue');
const HotVideo = () => import("@/views/content/HotVideoManage.vue");

const routes = [
    { path: "/", redirect: "" },
    {
        path: "",
        redirect: "/home",
        component: Index,
        meta: { requestAuth: true },
        children: [
            { path: '/home', component: Home, meta: { requestAuth: true } },
            { path: '/data', component: Data, meta: { requestAuth: true } },
            {
              path: '/content',
              redirect: '/content/carousel',
              children: [
                { path: '/content/carousel', component: Carousel, meta: { requestAuth: true } },
                { path: '/content/hot-search', component: HotSearch, meta: { requestAuth: true } },
                { path: '/content/tag', component: Tag, meta: { requestAuth: true } },
                { path: '/content/video', component: VideoManage, meta: { requestAuth: true } },
                { path: '/content/comment', component: CommentManage, meta: { requestAuth: true } },
                { path: '/content/hot-video', component: HotVideo, meta: { requestAuth: true } },
              ]
            },
            {
              path: '/review',
              redirect: '/review/video',
              children: [
                {
                  path: '/review/video',
                  redirect: '/review/video/form',
                  children: [
                    { path: '/review/video/form', component: Video, meta: { requestAuth: true } },
                    { path: '/review/video/detail/:vid', name: 'videoDetail', component: VideoDetail, meta: { requestAuth: true } },
                  ]
                },
              ]
            },
            { path: "/review/danmu", component: DanmuReview },
            {
              path: '/system',
              redirect: '/system/user',
              children: [
                { path: '/system/user', component: User, meta: { requestAuth: true } },
              ]
            },
            { path: '/system/info', component: SystemInfo, meta: { requestAuth: true } },
          ]
      },
    {
        path: "/login",
        name: "login",
        component: Login,
        meta: { requestAuth: false },
    },
    {
    path: "/:catchAll(.*)",
    component: NotFound,
    meta: { requestAuth: false },
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
  });
  
  // 本地没有token就跳到登录界面
  router.beforeEach((to, from, next) => {
    if (to.meta.requestAuth && !localStorage.getItem("token")) {
      next({ name: "login" });
    } else {
      next();
    }
  });
  
  export default router;