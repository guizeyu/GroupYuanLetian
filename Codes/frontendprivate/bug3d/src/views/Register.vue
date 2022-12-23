<template>
  <div id="form" class="animate__animated animate__fast animate__bounceInLeft">
    <div class="text-center container mt-5 col-sm-8 col-md-6 col-lg-4">
      <form>
        <img class="mb-4" src="welcome.png" alt="" width="100">
        <h1 class="h3 mb-3">一起来感受冬奥会！</h1>
        <div class="form-label-group">
          <restricted-text v-model="request.username" :validate="usernameValidate" id="username"
                           class="form-control user-select-none"
                           prompt="用户名必须由6至20位的大小写字母和数字组成"
                           placeholder="用户名" required="" autofocus=""/>
          <label class="user-select-none">用户名</label>
        </div>
        <div class="form-label-group">
          <restricted-text v-model="request.password" :validate="passwordValidate" id="password"
                           class="form-control user-select-none"
                           type="password" prompt="密码必须由6至20位的大小写字母、数字和特殊字符组成"
                           placeholder="密码" required="" autofocus=""/>
          <label class="user-select-none">密码</label>
        </div>
        <div class="form-label-group">
          <restricted-text v-model="request.password_repeat" :validate="passwordValidate" id="password_repeat"
                           class="form-control user-select-none"
                           type="password" prompt="密码必须由6至20位的大小写字母、数字和特殊字符组成"
                           placeholder="重复密码" required="" autofocus=""/>
          <label class="user-select-none">重复密码</label>
        </div>
        <div class="form-label-group">
          <restricted-text v-model="request.email" :validate="emailValidate" id="email"
                           class="form-control user-select-none"
                           type="text" prompt="请输入正确的邮箱"
                           placeholder="邮箱" required="" autofocus=""/>
          <label class="user-select-none">邮箱</label>
        </div>
        <div class="checkbox mb-3 mt-2">
          <p>已有账号？现在<a href="javascript:(0)" class="alert-link" @click.prevent="routeTo('login')">登陆</a>！</p>
        </div>
        <div v-if="!response.message.success" class="alert alert-danger" role="alert">
          {{ response.message.information }}
        </div>
        <button class="btn btn-lg btn-primary btn-block" type="submit" @click.prevent="doRegister">注册</button>
      </form>
    </div>
  </div>
  <modal ref="modal">注册成功</modal>
</template>

<script lang="ts">
import {defineComponent} from "vue"
import {
  RegisterRequest,
  RegisterResponse,
  passwordValidate,
  usernameValidate,
  emailValidate,
  register
} from "@/ts/register";
import RestrictedText from "@/components/RestrictedText.vue";
import Modal from "@/components/Modal.vue";

export default defineComponent({
  name: "RegisterView",
  components: {RestrictedText, Modal},
  data() {
    return {
      request: new RegisterRequest(),
      response: new RegisterResponse()
    }
  },
  methods: {
    passwordValidate,
    usernameValidate,
    emailValidate,
    routeTo(_name: string) {
      document.getElementById('form')!.classList.remove('animate__fast')
      document.getElementById('form')!.classList.remove('animate__bounceInLeft')
      document.getElementById('form')!.classList.add('animate__faster')
      document.getElementById('form')!.classList.add('animate__bounceOutRight')
      setTimeout(() => {
        this.$router.push({name: _name})
      }, 300)
    },
    doRegister()//注册
    {
      register(this.request).then(resp => {
        this.response = resp
        if (resp.message.success) {
          (this.$refs.modal as typeof Modal).show()
          this.$router.push({name: 'login'})
        }
      })
    }
  },
  unmounted()//用户跳出页面后，清除数据
  {
    this.request = new RegisterRequest()
    this.response = new RegisterResponse()
  }
})
</script>

<style scoped lang="scss">
@import "../scss/form";
</style>
