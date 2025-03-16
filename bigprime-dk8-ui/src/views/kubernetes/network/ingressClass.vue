<template>
  <div class="container-list">
    <Breadcrumb
      :items="[
        'menu.kubernetes',
        'menu.kubernetes.network',
        'menu.kubernetes.network.ingressclass'
      ]"
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
              <tiny-button size="small" @click="handleRefresh">刷新</tiny-button>
            </template>
          </tiny-grid-toolbar>
        </template>

        <tiny-grid-column type="index" width="50"></tiny-grid-column>

        <tiny-grid-column field="name" :title="$t('common.name')"></tiny-grid-column>

        <tiny-grid-column
          field="namespace"
          :title="$t('kubernetes.namespace')"
          width="180"
        ></tiny-grid-column>

        <tiny-grid-column
          field="controller"
          :title="$t('kubernetes.ingressClass.controller')"
          width="200"
        ></tiny-grid-column>

        <tiny-grid-column
          field="apiGroup"
          :title="$t('kubernetes.ingressClass.apiGroup')"
          width="180"
        ></tiny-grid-column>

        <tiny-grid-column
          field="scope"
          :title="$t('kubernetes.ingressClass.scope')"
          width="180"
        ></tiny-grid-column>

        <tiny-grid-column
          field="kind"
          :title="$t('kubernetes.ingressClass.kind')"
          width="180"
        ></tiny-grid-column>
      </tiny-grid>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Grid as TinyGrid, TinyButton, TinyGridColumn, TinyGridToolbar } from '@opentiny/vue'
import { iconDelete, iconEdit, iconEllipsis, iconEyeopen } from '@opentiny/vue-icon'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import { useI18n } from 'vue-i18n'
import { PageUtils } from '@/utils/page'
import { useClusterStore } from '@/store/modules/cluster'

const { t } = useI18n()
const clusterStore = useClusterStore()
const loading = ref(true)
const tableHeight = ref(500)
const tinyIconEllipsis = iconEllipsis()
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
const operations = ref([
  {
    //label: t('common.operations.edit'),
    name: 'view',
    icon: iconEyeopen()
  },
  {
    //label: t('common.operations.edit'),
    name: 'edit',
    icon: iconEdit()
  },
  {
    //label: t('common.operations.delete'),
    name: 'delete',
    icon: iconDelete()
  }
])
const rowBtnClick = (row, item) => {
  if (item.itemData.name === 'view') {
    viewConfig(row)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadDatas()
  tableHeight.value = PageUtils.setTableHeight(null)
})

const viewConfig = (node) => {
  let nodeName = node.name
  configData.value = source.value.filter((item: any) => (item.metadata.name = nodeName))[0]
  drawConfig.value.title = '【' + nodeName + '】 Config'
  drawConfig.value.visible = true
}

const loadDatas = async () => {
  try {
    loading.value = true
    const responseString = await Kubernetes.NetworkService.listIngressClass()
    if (responseString) {
      const response = JSON.parse(responseString)
      let fields = [] as any[]
      source.value = response.items
      if (response.items.length > 0) {
        response.items.forEach((item: any) => {
          let field = {} as any
          field.name = item.metadata.name
          field.namespace = item.metadata.namespace
          field.controller = item.spec.controller
          field.apiGroup = item.spec.apiGroup
          field.scope = item.spec.scope
          field.kind = item.kind
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
}
</style>
