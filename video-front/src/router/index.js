import { createRouter, createWebHistory } from 'vue-router'

const Index = () => import('../views/IndexView.vue')
const NotFound = () => import('@/views/NotFound.vue')
const Space = () => import('@/views/space/SpaceIndex.vue')
const SpaceHome = () => import('@/views/space/children/SpaceHome.vue')
const SpaceVideo = () => import('@/views/space/children/SpaceVideo.vue')
const SpaceArticle = () => import('@/views/space/children/SpaceArticle.vue')
const SpaceDynamic = () => import('@/views/space/children/SpaceDynamic.vue')
const SpaceFavlist = () => import('@/views/space/children/SpaceFavlist.vue')
const SpaceSetting = () => import('@/views/space/children/SpaceSetting.vue')
const SpaceFollow = () => import('@/views/space/children/SpaceFollow.vue')
const SpaceFans = () => import('@/views/space/children/SpaceFans.vue')
const Account = () => import('@/views/account/AccountView.vue')
const AccountHome = () => import('@/views/account/children/AccountHome.vue')
const AccountInfo = () => import('@/views/account/children/AccountInfo.vue')
const AccountAvatar = () => import('@/views/account/children/AccountAvatar.vue')
const AccountSecurity = () => import('@/views/account/children/AccountSecurity.vue')
const Platform = () => import('@/views/platform/PlatformView.vue')
const PlatformHome = () => import('@/views/platform/children/PlatformHome.vue')
const PlatformUpload = () => import('@/views/platform/children/PlatformUpload.vue')
const PlatformManuscript = () => import('@/views/platform/children/PlatformManuscript.vue')
const PlatformData = () => import('@/views/platform/children/PlatformData.vue')
const PlatformComment = () => import('@/views/platform/children/PlatformComment.vue')
const PlatformDanmu = () => import('@/views/platform/children/PlatformDanmu.vue')
const VideoUpload = () => import('@/views/platform/children/uploadChildren/VideoUpload.vue')
const VideoEdit = () => import('@/views/platform/children/uploadChildren/VideoEdit.vue')
const Message = () => import('@/views/message/MessageView.vue')
const MessageReply = () => import('@/views/message/children/MessageReply.vue')
const MessageLove = () => import('@/views/message/children/MessageLove.vue')
const MessageSystem = () => import('@/views/message/children/MessageSystem.vue')
const MessageWhisper = () => import('@/views/message/children/MessageWhisper.vue')
const WhisperDialog = () => import('@/views/message/children/children/WhisperDialog.vue')
const Search = () => import('@/views/search/SearchView.vue')
const SearchVideo = () => import('@/views/search/children/SearchVideo.vue')
const SearchUser = () => import('@/views/search/children/SearchUser.vue')
const VideoDetail = () => import('@/views/detail/VideoDetail.vue')


const routes = [
    { path: '/', redirect: '' },
    { path: '', name: "index", component: Index, meta: { requestAuth: false } },
    {
        path: '/space',
        component: Space,
        meta: { requestAuth: false },
        children: [
            { path: '/space/:uid', component: SpaceHome, meta: { requestAuth: false } },
            { path: '/space/:uid/video', component: SpaceVideo, meta: { requestAuth: false } },
            { path: '/space/:uid/article', component: SpaceArticle, meta: { requestAuth: false } },
            { path: '/space/:uid/dynamic', component: SpaceDynamic, meta: { requestAuth: false } },
            { path: '/space/:uid/favlist', component: SpaceFavlist, meta: { requestAuth: false }, props: route => ({ fid: route.query.fid }) },
            { path: '/space/:uid/setting', component: SpaceSetting, meta: { requestAuth: true } },
            { path: '/space/:uid/fans/follow', component: SpaceFollow, meta: { requestAuth: false } },
            { path: '/space/:uid/fans/fans', component: SpaceFans, meta: { requestAuth: false } },
        ]
    },
    {
        path: '/account',
        redirect: '/account/home',
        component: Account,
        meta: { requestAuth: true },
        children: [
            { path: '/account/home', component: AccountHome, meta: { requestAuth: true } },
            { path: '/account/info', component: AccountInfo, meta: { requestAuth: true } },
            { path: '/account/avatar', component: AccountAvatar, meta: { requestAuth: true } },
            { path: '/account/security', component: AccountSecurity, meta: { requestAuth: true } },
        ]
    },
    {
        path: '/platform',
        redirect: '/platform/home',
        component: Platform,
        children: [
            { path: '/platform/home', component: PlatformHome, meta: { requestAuth: true } },
            {
                path: '/platform/upload',
                component: PlatformUpload,
                redirect: '/platform/upload/video',
                children: [
                    { path: '/platform/upload/video', component: VideoUpload, meta: { requestAuth: true } },
                ]
            },
            {
                path: '/platform/edit',
                component: PlatformManuscript,
                redirect: '/platform/edit/video',
                children: [
                    { path: `/platform/edit/video/:vid`, component: VideoEdit, meta: { requestAuth: true } },
                ]
            },
            { path: '/platform/upload-manager', redirect: '/platform/upload-manager/manuscript' },
            { path: '/platform/upload-manager/manuscript', component: PlatformManuscript, meta: { requestAuth: true } },
            { path: '/platform/data-up', component: PlatformData, meta: { requestAuth: true } },
            { path: '/platform/comment', component: PlatformComment, meta: { requestAuth: true } },
            { path: '/platform/danmu', component: PlatformDanmu, meta: { requestAuth: true } },
        ]
    },
    {
        path: '/message',
        redirect: '/message/reply',
        component: Message,
        children: [
            { path: '/message/reply', component: MessageReply, meta: { requestAuth: true } },
            { path: '/message/love', component: MessageLove, meta: { requestAuth: true } },
            { path: '/message/system', component: MessageSystem, meta: { requestAuth: true } },
            {
                path: '/message/whisper', component: MessageWhisper, meta: { requestAuth: true },
                children: [
                    { path: '/message/whisper/:mid', component: WhisperDialog, meta: { requestAuth: true } }
                ]
            },
        ]
    },
    {
        path: '/search',
        component: Search,
        meta: { requestAuth: false },
        props: route => ({ keyword: route.query.keyword }),
        children: [
            { path: '/search/video', component: SearchVideo, meta: { requestAuth: false }, props: route => ({ keyword: route.query.keyword }) },
            { path: '/search/user', component: SearchUser, meta: { requestAuth: false }, props: route => ({ keyword: route.query.keyword }) },
        ]
    },
    { path: '/video/:vid', component: VideoDetail, meta: { requestAuth: false } },
    { path: '/:catchAll(.*)', name: "notfound", component: NotFound, meta: { requestAuth: false } },
    {
        path: '/v/:mcId',
        component: () => import('@/views/category/CategoryIndex.vue'),
        meta: { requestAuth: false },
    },
    {
        path: '/v/:mcId/:scId',
        component: () => import('@/views/category/CategoryIndex.vue'),
        meta: { requestAuth: false },
    },
    {
        path: '/v/popular',
        component: () => import('@/views/popular/Popular.vue'),
        meta: { requestAuth: false },
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    if (to.meta.requestAuth && !localStorage.getItem("token")) {
        next({ name: "index" });
    } else {
        next();
    }
});

export default router