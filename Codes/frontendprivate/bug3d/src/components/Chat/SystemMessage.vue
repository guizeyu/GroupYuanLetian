<template>
  <div class="system-message-container">
    <span class="system-message-content user-select-none">
      <span class="system-message-time font-weight-bold">[{{ this.time }}]</span>
      <slot></slot>
    </span>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue"

export default defineComponent({
  name: "SystemMessageComponent",
  props: ["time"],
  data() {
    return {}
  }
})
</script>
