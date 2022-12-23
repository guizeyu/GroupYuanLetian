<template>
  <div class="private-message-container">
    <span class="private-message-content user-select-none">
      <span class="private-message-time font-weight-bold">[{{ this.time }}]</span>
      <span>我对</span>
      <span class="private-message-user font-weight-bold">{{ this.to }}</span>
      <span>说:</span>
      <slot></slot>
    </span>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue"

export default defineComponent({
  name: "MyPrivateMessageComponent",
  props: ["to", "time"],
  data() {
    return {}
  }
})
</script>
