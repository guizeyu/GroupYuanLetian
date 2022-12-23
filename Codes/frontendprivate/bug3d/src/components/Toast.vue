<template>
  <div class="position-fixed bottom-0 right-0 p-3" style="z-index: 5; right: 0; bottom: 0;">
    <div id="liveToast" class="toast hide" role="alert" aria-live="assertive" aria-atomic="true" data-delay="2000">
      <div class="toast-header">
        <svg class="bd-placeholder-img rounded mr-2" width="20" height="20" xmlns="http://www.w3.org/2000/svg"
             role="img" aria-label=" :  " preserveAspectRatio="xMidYMid slice" focusable="false">
          <title></title>
          <rect width="100%" height="100%" :fill="color"></rect>
          <text x="50%" y="50%" fill="#dee2e6" dy=".3em"></text>
        </svg>
        <strong class="mr-auto">智慧3D冬奥博物馆&nbsp;&nbsp;&nbsp;&nbsp;</strong>
        <small>{{ time }}</small>
        <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="toast-body">
        {{ msg }}
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue"
import $ from 'jquery'

export default defineComponent({
  name: "ToastComponent",
  data()
  {
    return {
      color: "",
      msg: "",
      time: ""
    }
  },
  methods: {
    getTime(): string
    {
      let date: Date = new Date()
      return date.toLocaleTimeString()
    },
    success(msg: string): void
    {
      this.time = this.getTime()
      this.color = "#00a000"
      this.msg = msg
      $('.toast').toast('show')
    },
    alert(msg: string): void
    {
      this.time = this.getTime()
      this.color = "#ff0000"
      this.msg = msg
      $('.toast').toast('show')
    },
    info(msg: string): void
    {
      this.time = this.getTime()
      this.color = "#007aff"
      this.msg = msg
      $('.toast').toast('show')
    }
  }
})
</script>
