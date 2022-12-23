<template>
  <div class="user-message-container">
    <span class="user-message-content user-select-none">
      <span class="user-message-time font-weight-bold">[{{ this.time }}]</span>
      <span class="user-message-user font-weight-bold">{{ this.from }}</span>:
      <slot></slot>
    </span>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue"

export default defineComponent({
  name: "UserMessageComponent",
  props: ["from", "time"],
  data() {
    return {}
  }
})
</script>
