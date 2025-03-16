<template>
  <tiny-dialog-box
    :visible="visible"
    fullscreen
    @update:visible="visible = $event"
    title="资源创建"
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    :show-close="false"
    class="resource-dialog"
  >
    <tiny-form :model="formData" label-width="100px" label-position="top">
      <tiny-form-item label="资源类型" prop="resType" v-show="false">
        <tiny-input type="text" v-model="formData.resType"></tiny-input>
      </tiny-form-item>
      <tiny-form-item prop="yaml">
        <monacoEditor v-model="formData.yaml" language="yaml" theme="vs" ></monacoEditor>
      </tiny-form-item>
    </tiny-form>
    <template #footer>
      <tiny-button @click="handlerCancel"> 取消</tiny-button>
      <tiny-button type="primary" @click="handleSubmit"> 创建</tiny-button>
    </template>
  </tiny-dialog-box>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Modal, TinyButton, TinyDialogBox, TinyForm, TinyFormItem, TinyInput, TinyNotify } from '@opentiny/vue'
import MonacoEditor from '@/components/monaco-editor/monaco-editor.vue'
import emitter from '@/utils/evnetbus'
import { Kubernetes } from '@/services/kubernetes/kubernetes'

const visible = defineModel('visible', { default: false })
const resourceType = defineModel('resourceType', { default: '' })
const yaml = defineModel('yaml', { default: '' })

const formData = ref({
  resType: resourceType,
  yaml: yaml
})

const handleSubmit = async () => {
  try {
    const resp = await Kubernetes.ResourceService.resourceCreate({
      resType: formData.value.resType,
      yaml: formData.value.yaml
    })
    if (resp.startsWith('Success')) {
      TinyNotify({
        type: 'success',
        message: '资源创建成功',
        duration: 2000,
        position: 'top-right'
      })
      handlerCancel()
      emitter.emit('handleRefresh')
    } else {
      Modal.message({ status: 'error', message: resp, top: 20 })
    }
  } catch (error) {
    Modal.message({ status: 'error', message: error, top: 20 })
  }
}

function handlerCancel() {
  visible.value = false
  emitter.emit('handleCreateClose')
}
</script>

<style scoped lang="less">
.resource-dialog {
  :deep(.tiny-dialog-box .tiny-dialog-box__body) {
    max-height: 100%;
  }

  .codeEditBox {
    height: calc(100vh - 210px);
  }
}

.tiny-notify {
  overflow-y: scroll;
}
</style>
