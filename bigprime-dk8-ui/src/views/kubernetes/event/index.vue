<template>
  <div class="container-list">
    <Breadcrumb
      v-if="isShowBreadcrumb"
      :items="['menu.kubernetes', 'menu.kubernetes.events']"
      :title="clusterStore.clusterInfo.clusterName"
    />
    <div class="contain">
      <tiny-grid
        :data="gridData"
        :fit="true"
        size="small"
        border
        :stripe="true"
        show-header-overflow="tooltip"
        show-overflow="tooltip"
        highlight-hover-row
        :height="tableHeight"
        :loading="loading"
      >
        <template #toolbar>
          <tiny-grid-toolbar class="grid-toolbar" size="small" setting full-screen>
            <template #buttons>
              <tiny-select
                v-model="selectNs"
                input-box-type="underline"
                clearable
                size="small"
                @change="nsChange"
                :placeholder="$t('kubernetes.search.namespace')"
              >
                <tiny-option v-for="item in namespaces" :key="item" :label="item" :value="item">
                </tiny-option>
              </tiny-select>
              <tiny-button size="small" @click="handleRefresh">刷新</tiny-button>
            </template>
          </tiny-grid-toolbar>
        </template>

        <tiny-grid-column type="index" width="60" fixed="left"></tiny-grid-column>

        <tiny-grid-column
          field="type"
          :title="$t('common.type')"
          width="90"
          fixed="left"
        ></tiny-grid-column>

        <tiny-grid-column
          field="message"
          :title="$t('kubernetes.event.message')"
          min-width="300"
          fixed="left"
        >
          <template #default="data">
            <span v-if="data.row.type == 'Warning'" style="color: coral">{{
              data.row.message
            }}</span>
            <span v-else-if="data.row.type == 'Error'" style="color: orangered">{{
              data.row.message
            }}</span>
            <span v-else>{{ data.row.message }}</span>
          </template>
        </tiny-grid-column>

        <tiny-grid-column
          field="namespace"
          :title="$t('kubernetes.namespace')"
          width="120"
        ></tiny-grid-column>

        <tiny-grid-column
          field="involvedObject"
          :title="$t('kubernetes.event.involvedObject')"
          min-width="200"
        >
          <template #default="data">
            <span style="color: #1a78c4">{{ data.row.involvedObject }}</span>
          </template>
        </tiny-grid-column>

        <tiny-grid-column
          field="source"
          :title="$t('kubernetes.event.source')"
          min-width="256"
        ></tiny-grid-column>

        <tiny-grid-column
          field="reason"
          :title="$t('kubernetes.event.reason')"
          min-width="180"
        ></tiny-grid-column>

        <tiny-grid-column
          field="count"
          :title="$t('kubernetes.event.count')"
          width="80"
          align="center"
        ></tiny-grid-column>

        <tiny-grid-column field="createAt" :title="$t('kubernetes.age')" align="center" width="100">
        </tiny-grid-column>
      </tiny-grid>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  Grid as TinyGrid,
  TinyButton,
  TinyGridColumn,
  TinyGridToolbar,
  TinyOption,
  TinySelect
} from '@opentiny/vue'
import { useI18n } from 'vue-i18n'
import { getDayAndHours } from '@/utils/date'
import { PageUtils } from '@/utils/page'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import { useClusterStore } from '@/store/modules/cluster'

const { t } = useI18n()
const clusterStore = useClusterStore()
const loading = ref(true)
const tableHeight = ref(500)
const height = defineModel('height', { default: 0 })
const isShowBreadcrumb = defineModel('isShowBreadcrumb', { default: true })
const namespaces = ref([])
const selectNs = ref('')
const gridData = ref<any[]>([])
const source = ref([])
const configData = ref()
const drawConfig = ref({
  title: '',
  visible: false,
  width: '70%',
  handleDrawerClose: () => {
    drawConfig.value.visible = false
    configData.value = {}
  }
})

// 组件挂载时加载数据
onMounted(() => {
  loadNamespaces()
  loadDatas()
  tableHeight.value =
    height.value == 0 ? PageUtils.setTableHeight(null) : PageUtils.setTableHeight(height.value)
})

const viewConfig = (node: any) => {
  let nodeName = node.name
  configData.value = source.value.filter((item: any) => (item.metadata.name = nodeName))[0]
  drawConfig.value.title = '【' + nodeName + '】 Config'
  drawConfig.value.visible = true
}

const formatTimestamp = (creationTimestamp: any) => {
  if (creationTimestamp) {
    const date = new Date(creationTimestamp)
    return getDayAndHours(date.toLocaleString())
  }
  return creationTimestamp
}

const nsChange = async () => {
  await loadDatas()
}

const loadNamespaces = async () => {
  const ns = await Kubernetes.NamespaceService.listNamespace()
  namespaces.value = ns.items.map((n: any) => {
    return n.metadata.name
  })
  console.log(namespaces.value)
}

const loadDatas = async () => {
  try {
    loading.value = true
    const responseString = await Kubernetes.EventService.listEvent(selectNs.value)
    if (responseString) {
      const response = JSON.parse(responseString)
      let fields = [] as any[]
      source.value = response.items
      if (response.items.length > 0) {
        response.items.forEach((item: any) => {
          let field = {} as any
          field.type = item.type
          field.message = item.message || item.action
          field.namespace = item.metadata.namespace
          if (item.involvedObject) {
            field.involvedObject = item.involvedObject.kind + '：' + item.involvedObject.name
          }
          if (item.source) {
            field.source = item.source.component + ' ' + item.source.host
          }
          field.count = 0
          if (item.count) {
            field.count = item.count
          }
          if (item.reason) {
            field.reason = item.reason
          }
          field.createAt = formatTimestamp(item.metadata.creationTimestamp)
          fields.push(field)
        })
        if (fields.length > 0) {
          gridData.value = fields
        }
      } else {
        gridData.value = []
      }
    }
    loading.value = false
  } catch (error) {
    loading.value = false
    console.error('Failed to load namespaces:', error)
  }
}

const handleRefresh = async () => {
  await loadDatas()
}
</script>

<style scoped lang="less">
.container-list {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
}

.contain {
  flex: 1 1 auto;
  margin: 8px 10px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 0 8px 8px rgba(169, 174, 184, 0.05);
  padding: 10px;

  .grid-toolbar {
    .tiny-select {
      width: 280px;
    }
  }
}
</style>
