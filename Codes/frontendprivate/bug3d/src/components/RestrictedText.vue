<template>
  <input :value="v" @input="vc($event.target.value)" type="text" data-container="body" data-toggle="popover"
         data-trigger="focus" data-placement="bottom" :data-content="prompt" :id="id"/>
</template>

<script lang="ts">
import {defineComponent} from "vue"
import $ from 'jquery'

export default defineComponent({
  name: "RestrictedTextComponent",
  props: ["id", "validate", "modelValue", "prompt"],
  emits: ['update:modelValue'],
  data()
  {
    return {v: ""}
  },
  methods: {
    vc(v: string)
    {
      const id = `#${this.id}`
      this.v = v
      this.$emit('update:modelValue', this.v)
      if (this.validate(v))
      {
        $(id).popover('hide')
      }
      else
      {
        $(id).popover('show')
      }
    }
  },
  created()
  {
    this.$watch('modelValue', (oldValue: any, newValue: any) =>
    {
      this.v = this.modelValue
    })
  }
})
</script>
