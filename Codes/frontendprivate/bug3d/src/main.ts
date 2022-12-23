import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import * as THREE from 'three'
import $ from 'jquery'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.min'
import animated from 'animate.css'

const app = createApp(App).use(animated).use(store).use(router)
router.isReady().then(() => app.mount('#app'))
