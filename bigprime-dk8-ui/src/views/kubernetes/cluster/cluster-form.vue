<template>
  <div @click="handlerBack" style="width: 60px">
    <tiny-tag type="info" style="cursor: pointer">
      <IconArrowLeft />
      <span>返回</span>
    </tiny-tag>
  </div>
  <div class="cluster-add-form">
    <tiny-form label-width="100px" ref="formRef" :model="formData" :rules="rules">
      <tiny-form-item label="集群标识">
        <tiny-input v-model="formData.clusterId" disabled></tiny-input>
      </tiny-form-item>
      <tiny-form-item label="集群名称" prop="clusterName">
        <tiny-input v-model="formData.clusterName"></tiny-input>
      </tiny-form-item>
      <tiny-form-item label="集群配置" prop="configContent">
        <tiny-input
          v-model="formData.configContent"
          type="textarea"
          rows="15"
          placeholder="粘贴kube.config文件内容"
        ></tiny-input>
      </tiny-form-item>
      <tiny-form-item style="text-align: right">
        <tiny-button @click="handlerFormCancel"> 取消</tiny-button>
        <tiny-button type="primary" @click="handlerFormSubmit"> 确定</tiny-button>
      </tiny-form-item>
    </tiny-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import emitter from '@/utils/evnetbus'
import { Modal, TinyButton, TinyForm, TinyFormItem, TinyInput, TinyTag } from '@opentiny/vue'
import { iconArrowLeft } from '@opentiny/vue-icon'
import { Kubernetes } from '@/services/kubernetes/kubernetes'

const formRef = ref()
const IconArrowLeft = iconArrowLeft()
const propModel = defineModel('currentRow', {
  default: { clusterId: '', clusterName: '', configContent: '' }
})
const formData = reactive({
  clusterId: propModel.value.clusterId,
  clusterName: propModel.value.clusterName,
  configContent: propModel.value.configContent
})

const rules = ref({
  clusterName: [{ required: true, message: '必填', trigger: 'blur' }],
  configContent: [{ required: true, message: '必填', trigger: 'blur' }]
})

const handlerBack = () => {
  formRef.value.resetFields()
  emitter.emit('handleBack')
}

const handlerFormCancel = () => {
  handlerBack()
}

const handlerFormSubmit = () => {
  formRef.value.validate((valid: any) => {
    if (valid) {
      Kubernetes.ClusterService.registerCluster({
        clusterId: formData.clusterId,
        clusterName: formData.clusterName,
        configContent: formData.configContent
      }).then((resp) => {
        if (resp === true) {
          formRef.value.resetFields()
          Modal.message({ message: '添加成功' })
          emitter.emit('handleBack')
        }
      })
    }
  })
}
</script>

<style scoped lang="less">
.cluster-add-form {
  margin: auto;
  width: 800px;
}
</style>
