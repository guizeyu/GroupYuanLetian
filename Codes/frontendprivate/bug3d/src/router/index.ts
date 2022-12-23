import {createRouter, createWebHistory, RouteRecordRaw} from 'vue-router'
import store from "@/store";

const routes: Array<RouteRecordRaw> = [
    {
        path: '/login',
        name: 'login',
        alias: '/',
        component: () => import('../views/Login.vue'),
        meta: {title: '登陆', auth: false}
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('../views/Register.vue'),
        meta: {title: '注册', auth: false}
    },
    {
        path: '/museum',
        name: 'museum',
        component: () => import('../views/Museum.vue'),
        meta: {title: '博物馆', auth: true}
    },
    {
        path: '/slope',
        name: 'slope',
        component: () => import('../views/Slope.vue'),
        meta: {title: '滑雪场', auth: true}
    },
    {
        path: '/curling',
        name: 'curling',
        component: () => import('../views/Curling.vue'),
        meta: {title: '冰壶馆', auth: true}
    },
    {
        path: '/chooseCharacter',
        name: 'chooseCharacter',
        component: () => import('../views/ChooseCharacter.vue'),
        meta: {title: '选择形象', auth: true}
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

//浏览器窗口标题
router.beforeEach((to, from, next) => {
    document.title = (to.meta.title == undefined ? "Vue app" : to.meta.title) as string
    if (to.meta.auth && !store.getters.isLogin) //鉴权
        next({path: '/login'})
    else
        next();
})
export default router
