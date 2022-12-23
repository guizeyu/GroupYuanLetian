<template>
  <div class="private-message-container">
    <span class="private-message-content user-select-none">
      <span class="private-message-time font-weight-bold">[{{ this.time }}]</span>
      <span class="private-message-user font-weight-bold">{{ this.from }}</span>
      <span>对你说:</span>
      <slot></slot>
    </span>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue"

export default defineComponent({
  name: "HisPrivateMessageComponent",
  props: ["from", "time"],
  data() {
    return {}
  }
})
</script>
