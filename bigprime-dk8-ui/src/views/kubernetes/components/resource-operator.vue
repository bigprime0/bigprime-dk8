<template>
  <div class="operator-title">
    <tiny-tag type="success" effect="dark" size="small" @click="handleBack" style="cursor: pointer">
      <IconArrowLeft />
      <span>返回</span>
    </tiny-tag>
    <tiny-tag size="small">
      <span>{{ propModel.namespace }}</span>
    </tiny-tag>
    <tiny-tag size="small">
      <span> {{ propModel.name }}</span>
    </tiny-tag>
  </div>
  <div class="operator-tab">
    <tiny-tabs v-model="activeName" separator size="small" v-if="showLog">
      <tiny-tab-item :title="$t('kubernetes.pod.operator.descr')" lazy name="descr">
        <tiny-alert
          size="large"
          title="提示：可以在编辑框修改内容来更新Kubernetes资源"
          class="tag-alert"
          :closable="false"
        >
          <a href="javascript: void(0);" @click="handlerUpdate">提交</a>
        </tiny-alert>
        <div class="edit-content">
          <!--          <VueJsonPretty
                      :data="detailModel"
                      :deep="2"
                      :showLine="true"
                      :showLineNumber="true"
                      :virtual="false"
                      :showIcon="true"
                      :height="jsonHeight"
                      :editable="true"
                      editableTrigger="click"
                      theme="light"
                      :showDoubleQuotes="true"
                    ></VueJsonPretty>-->
          <monacoEditor
            :model-value="detailContent"
            @change="handlerChange"
            language="json"
            theme="vs"
            fontSize="12"
            minimap="false"
          ></monacoEditor>
        </div>
      </tiny-tab-item>
      <tiny-tab-item :title="$t('kubernetes.pod.operator.log')" lazy name="log">
        <div class="log-content">
          <VueJsonPretty :data="logs" :showLineNumber="true" :height="jsonHeight"></VueJsonPretty>
        </div>
      </tiny-tab-item>
    </tiny-tabs>
    <div v-else>
      <tiny-alert
        size="large"
        title="提示：可以在编辑框修改内容来更新Kubernetes资源"
        class="tag-alert"
        :closable="false"
      >
        <a href="javascript: void(0);" @click="handlerUpdate">提交</a>
      </tiny-alert>
      <monacoEditor
        :model-value="detailContent"
        @change="handlerChange"
        language="json"
        theme="vs"
      ></monacoEditor>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Modal, TinyAlert, TinyTabItem, TinyTabs, TinyTag } from '@opentiny/vue'
import { iconArrowLeft } from '@opentiny/vue-icon'
import emitter from '@/utils/evnetbus'
import { useI18n } from 'vue-i18n'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'
import { PageUtils } from '@/utils/page'
import MonacoEditor from '@/components/monaco-editor/monaco-editor.vue'

const { t } = useI18n()
const IconArrowLeft = iconArrowLeft()
const activeName = ref('descr')
const resType = defineModel<string>('resType', { default: '' })
const propModel = defineModel<any>('currentRow', { default: {} })
const detailModel = defineModel<any>('descr', { default: {} })
const showLog = defineModel<boolean>('showLog', { default: false })
const detailContent = ref('')
const logs = ref<string[]>([])
const jsonHeight = ref(500)

onMounted(() => {
  if (showLog.value) {
    getResourceLogs()
  }
  jsonHeight.value = PageUtils.setTableHeight(300)
  detailContent.value = JSON.stringify(detailModel.value, null, 2)
})

const handleBack = () => {
  emitter.emit('handleBack')
}

const handlerChange = (newValue: string) => {
  detailContent.value = newValue
}

const getResourceLogs = async () => {
  try {
    const respLogs = await Kubernetes.ResourceService.getResourceLogs({
      namespace: propModel.value.namespace,
      resName: propModel.value.name,
      resType: resType.value
    })
    if (respLogs) {
      respLogs.split('\n').forEach((line: string) => {
        if (line.length > 0) {
          logs.value.push(line)
        }
      })
    }
  } catch (error) {
    logs.value.push('Failed to load namespaces:' + error)
  }
}

const handlerUpdate = async () => {
  try {
    Modal.confirm({
      title: '更新确认',
      message: '确定要执行' + resType.value + '[' + propModel.value.name + ']的资源更新吗？',
      showHeader: true,
      showFooter: true,
      resize: true,
      events: {
        confirm(ev: any) {
          Kubernetes.ResourceService.resourceUpdate({
            namespace: propModel.value.namespace,
            resName: propModel.value.name,
            resType: resType.value,
            content: detailContent.value
          }).then((response) => {
            if (response.startsWith('Failure')) {
              Modal.message({ status: 'error', message: response, top: 20 })
            } else {
              Modal.message({ status: 'success', message: '更新成功!', top: 20 })
              emitter.emit('handleRefresh')
            }
          })
        }
      }
    })
  } catch (error) {
    Modal.message({ status: 'error', message: error, top: 20 })
  }
}
</script>

<style scoped lang="less">
.operator-title {
  .tiny-tag {
    margin-right: 5px;
  }
}

.tag-alert {
  margin-bottom: 10px;
}

.edit-content {
  height: calc(100vh - 300px);
  overflow-y: scroll;
  border: 1px solid #eee;
}

.log-content {
  height: calc(100vh - 210px);
  overflow-y: scroll;
  border: 1px solid #eee;

  .log-line {
    font-size: 13px;
  }

  .vjs-tree {
    height: calc(100vh - 235px);
  }
}

.codeEditBox {
  height: calc(100vh - 325px);
}
</style>
