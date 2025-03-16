<template>
  <tiny-dialog-box
    :visible="visible"
    fullscreen
    @update:visible="visible = $event"
    title="查看明细"
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    :show-close="false"
    class="resource-dialog"
  >
    <VueJsonPretty
      :data="content"
      :deep="2"
      :showLineNumber="true"
      :virtual="false"
      :showIcon="true"
      :height="jsonHeight"
      theme="light"
      :showDoubleQuotes="true"
    ></VueJsonPretty>
    <template #footer>
      <tiny-button @click="handlerClose"> 关闭</tiny-button>
    </template>
  </tiny-dialog-box>
</template>

<script setup lang="ts">
import { TinyButton, TinyDialogBox } from '@opentiny/vue'
import emitter from '@/utils/evnetbus'
import VueJsonPretty from 'vue-json-pretty'
import { onMounted, ref } from 'vue'
import { PageUtils } from '@/utils/page'
import 'vue-json-pretty/lib/styles.css'

const jsonHeight = ref(500)
const visible = defineModel('visible', { default: false })
const content = defineModel('content', { default: '' })

onMounted(() => {
  jsonHeight.value = PageUtils.setTableHeight(null)
})

function handlerClose() {
  visible.value = false
  emitter.emit('handleViewClose')
}
</script>

<style scoped lang="less">
.resource-dialog {
  :deep(.tiny-dialog-box .tiny-dialog-box__body) {
    max-height: 100%;
  }

  .codeEditBox {
    height: calc(100vh - 200px);
  }
}
</style>
