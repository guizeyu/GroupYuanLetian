import {createStore} from 'vuex'

const SERVER_IP = "124.221.255.142"
const SERVER_PORT = "62105"
export const UPDATE_USERNAME = 'updateUsername'
export const UPDATE_TOKEN = 'updateToken'
export const UPDATE_SCENE = 'updateScene'
export const UPDATE_CHARACTER = 'updateCharacter'
export const UPDATE_MUSEUM_INTERACTION = 'updateMuseumInteraction'
export const UPDATE_BEST_SKI_TIME = 'updateBestSkiTime'
export const UPDATE_BEST_CURLING_SCORE = 'updateBestCurlingScore'

export default createStore({
    state: {
        username: "",
        token: "",
        scene: 1, //上一次选择的场景
        character: 1, //人物形象
        serverIP: SERVER_IP,
        serverPort: SERVER_PORT,
        museumInteraction: false, //博物馆交互
        bestSkiTime: 10000, //最佳滑冰时间
        bestCurlingScore: 0 //最佳冰壶分数
    },
    getters: {
        isLogin(state, getters): boolean {
            return getters.token.length > 0
        },
        scene(state): number {
            if (window.sessionStorage.getItem('scene') != null &&
                typeof (window.sessionStorage.getItem('scene')) != 'undefined') {
                state.scene = Number(<string>(window.sessionStorage.getItem('scene')))
            }
            return state.scene
        },
        character(state): number {
            if (window.localStorage.getItem(state.username + '_character') != null &&
                typeof (window.localStorage.getItem(state.username + '_character')) != 'undefined') {
                state.character = Number(<string>(window.localStorage.getItem(state.username + '_character')))
            }
            return state.character
        },
        museumInteraction(state): boolean {
            if (window.localStorage.getItem(state.username + '_museumInteraction') != null &&
                typeof (window.localStorage.getItem(state.username + '_museumInteraction')) != 'undefined') {
                state.museumInteraction = Boolean(<string>(window.localStorage.getItem(state.username + '_museumInteraction')))
            }
            return state.museumInteraction
        },
        bestSkiTime(state): number {
            if (window.localStorage.getItem(state.username + '_bestSkiTime') != null &&
                typeof (window.localStorage.getItem(state.username + '_bestSkiTime')) != 'undefined') {
                state.bestSkiTime = Number(<string>(window.localStorage.getItem(state.username + '_bestSkiTime')))
            }
            return state.bestSkiTime
        },
        bestCurlingScore(state): number {
            if (window.localStorage.getItem(state.username + '_bestCurlingScore') != null &&
                typeof (window.localStorage.getItem(state.username + '_bestCurlingScore')) != 'undefined') {
                state.bestCurlingScore = Number(<string>(window.localStorage.getItem(state.username + '_bestCurlingScore')))
            }
            return state.bestCurlingScore
        },
        token(state): string {
            if (window.sessionStorage.getItem('token') != null &&
                typeof (window.sessionStorage.getItem('token')) != 'undefined' &&
                (<string>(window.sessionStorage.getItem('token'))).length > 0) {
                state.token = <string>(window.sessionStorage.getItem('token'))
            }
            return state.token
        },
        username(state): string {
            if (window.sessionStorage.getItem('username') != null &&
                typeof (window.sessionStorage.getItem('username')) != 'undefined' &&
                (<string>(window.sessionStorage.getItem('username'))).length > 0) {
                state.username = <string>(window.sessionStorage.getItem('username'))
            }
            return state.username
        },
        baseURL(state): string {
            return state.serverIP + ":" + state.serverPort
        },
        httpURL(state, getters): string {
            return "http://" + getters.baseURL
        },
        websocketURL(state, getters): string {
            return "ws://" + getters.baseURL + "/ws"
        }
    },
    mutations: {
        [UPDATE_USERNAME](state, username: string) {
            state.username = username
            window.sessionStorage.setItem('username', username)
        },
        [UPDATE_TOKEN](state, token: string) {
            state.token = token
            window.sessionStorage.setItem('token', token)
        },
        [UPDATE_SCENE](state, scene: number) {
            state.scene = scene
            window.sessionStorage.setItem('scene', String(scene))
        },
        [UPDATE_CHARACTER](state, character: number) {
            state.character = character
            window.localStorage.setItem(state.username + '_character', String(character))
        },
        [UPDATE_MUSEUM_INTERACTION](state, museumInteraction: boolean) {
            state.museumInteraction = museumInteraction
            window.localStorage.setItem(state.username + '_museumInteraction', String(museumInteraction))
        },
        [UPDATE_BEST_SKI_TIME](state, bestSkiTime: number) {
            state.bestSkiTime = bestSkiTime
            window.localStorage.setItem(state.username + '_bestSkiTime', String(bestSkiTime))
        },
        [UPDATE_BEST_CURLING_SCORE](state, bestCurlingScore: number) {
            state.bestCurlingScore = bestCurlingScore
            window.localStorage.setItem(state.username + '_bestCurlingScore', String(bestCurlingScore))
        }
    },
    actions: {
        [UPDATE_USERNAME]({commit, state}, username: string) {
            commit(UPDATE_USERNAME, username)
        },
        [UPDATE_TOKEN]({commit, state}, token: string) {
            commit(UPDATE_TOKEN, token)
        },
        [UPDATE_SCENE]({commit, state}, scene: number) {
            commit(UPDATE_SCENE, scene)
        },
        [UPDATE_CHARACTER]({commit, state}, character: number) {
            commit(UPDATE_CHARACTER, character)
        },
        [UPDATE_MUSEUM_INTERACTION]({commit, state}, museumInteraction: boolean) {
            commit(UPDATE_MUSEUM_INTERACTION, museumInteraction)
        },
        [UPDATE_BEST_SKI_TIME]({commit, state}, bestSkiTime: number) {
            commit(UPDATE_BEST_SKI_TIME, bestSkiTime)
        },
        [UPDATE_BEST_CURLING_SCORE]({commit, state}, bestCurlingScore: number) {
            commit(UPDATE_BEST_CURLING_SCORE, bestCurlingScore)
        }
    },
    modules: {}
})
