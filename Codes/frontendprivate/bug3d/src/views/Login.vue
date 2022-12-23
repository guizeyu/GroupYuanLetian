<template>
  <div id="form" class="animate__animated animate__fast animate__bounceInLeft">
    <div class="text-center container mt-5 col-sm-8 col-md-6 col-lg-4">
      <form>
        <img class="mb-4" src="welcome.png" alt="" width="100">
        <h1 class="h3 mb-3">一起来感受冬奥会！</h1>
        <div class="form-label-group">
          <input v-model="request.username" type="text" id="username" class="form-control user-select-none"
                 placeholder="用户名" required="" autofocus="" autocomplete="off">
          <label class="user-select-none" for="username">用户名</label>
        </div>
        <div class="form-label-group">
          <input v-model="request.password" type="password" id="password" class="form-control user-select-none"
                 placeholder="密码" required="" autocomplete="off">
          <label class="user-select-none" for="password">密码</label>
        </div>
        <div class="mb-3 mt-2">
          <p>没有账号？现在<a class="alert-link" href="javascript:(0)" @click.prevent="routeTo('register')">注册</a>！</p>
          <div v-if="!response.message.success" class="alert alert-danger"
               role="alert">
            账号或密码错误！
          </div>
        </div>
        <button @click.prevent="doLogin" class="btn btn-lg btn-primary btn-block" type="submit">登陆</button>
        <p class="mt-5 mb-3 text-muted">alpha 1.00 © 2022-2022</p>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import {login, LoginRequest, LoginResponse} from "@/ts/login"
import {defineComponent} from "vue"
import {mapMutations} from "vuex";
import {UPDATE_TOKEN, UPDATE_USERNAME} from "@/store";

export default defineComponent({
  name: "LoginView",
  data() {
    return {
      request: new LoginRequest(),
      response: new LoginResponse()
    }
  },
  methods: {
    routeTo(_name: string) {
      document.getElementById('form')!.classList.remove('animate__fast')
      document.getElementById('form')!.classList.remove('animate__bounceInLeft')
      document.getElementById('form')!.classList.add('animate__faster')
      document.getElementById('form')!.classList.add('animate__bounceOutRight')
      setTimeout(() => {
        this.$router.push({name: _name})
      }, 300)
    },
    doLogin() {
      login(this.request).then(resp => {
        this.response = resp
        if (resp.message.success) {
          this.updateToken(resp.token)
          this.updateUsername(this.request.username)
          this.routeTo("chooseCharacter")
        }
      })
    },
    ...mapMutations([
      UPDATE_TOKEN, UPDATE_USERNAME
    ])
  },
  unmounted()//用户跳出页面后，清除数据
  {
    this.request = new LoginRequest()
    this.response = new LoginResponse()
  }
})
</script>

<style scoped lang="scss">
@import "../scss/form";
</style>
