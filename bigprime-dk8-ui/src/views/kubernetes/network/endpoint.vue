<template>
  <div class="container-list">
    <Breadcrumb
      :items="['menu.kubernetes', 'menu.kubernetes.network', 'menu.kubernetes.network.endpoint']"
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
        @cell-click="handleShowValue"
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

        <tiny-grid-column type="index" width="50"></tiny-grid-column>

        <tiny-grid-column field="name" :title="$t('common.name')"></tiny-grid-column>

        <tiny-grid-column
          field="namespace"
          :title="$t('kubernetes.namespace')"
          width="180"
        ></tiny-grid-column>

        <tiny-grid-column field="endpoints" :title="$t('kubernetes.service.endpoints')" width="200">
          <template #default="data">
            <div v-for="item in data.row.endpoints" :key="item.ip">
              <tiny-tag size="small" type="info">{{ item.ip }}:{{ item.port }}</tiny-tag>
            </div>
          </template>
        </tiny-grid-column>

        <tiny-grid-column
          field="createAt"
          :title="$t('kubernetes.age')"
          align="center"
          width="120"
        ></tiny-grid-column>
      </tiny-grid>
    </div>
    <tiny-drawer
      :title="drawConfig.title"
      :visible="drawConfig.visible"
      @update:visible="drawConfig.visible = $event"
      :show-header="true"
      :mask-closable="false"
      :width="drawConfig.width"
      @close="drawConfig.handleDrawerClose"
    >
      <!--      <ShowConfigEditor :key="nanoid(8)" :show-data="configData"></ShowConfigEditor>-->
    </tiny-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  Grid as TinyGrid,
  TinyActionMenu,
  TinyButton,
  TinyDrawer,
  TinyGridColumn,
  TinyGridToolbar,
  TinyOption,
  TinySelect,
  TinyTag
} from '@opentiny/vue'
import { iconDelete, iconEdit, iconEllipsis, iconEyeopen } from '@opentiny/vue-icon'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import { useI18n } from 'vue-i18n'
import { getDayAndHours } from '@/utils/date'
import { PageUtils } from '@/utils/page'
import { useClusterStore } from '@/store/modules/cluster'

const { t } = useI18n()
const clusterStore = useClusterStore()
const loading = ref(true)
const tableHeight = ref(500)
const tinyIconEllipsis = iconEllipsis()
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
  loadNamespaces()
  loadDatas()
  tableHeight.value = PageUtils.setTableHeight(null)
})

const viewConfig = (node) => {
  let nodeName = node.name
  configData.value = source.value.filter((item: any) => (item.metadata.name = nodeName))[0]
  drawConfig.value.title = '【' + nodeName + '】 Config'
  drawConfig.value.visible = true
}

const formatTimestamp = (creationTimestamp) => {
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
  namespaces.value = ns.items.map((n) => {
    return n.metadata.name
  })
  console.log(namespaces.value)
}

const loadDatas = async () => {
  try {
    loading.value = true
    const responseString = await Kubernetes.NetworkService.listEndpoint(selectNs.value)
    if (responseString) {
      const response = JSON.parse(responseString)
      let fields = [] as any[]
      source.value = response.items
      if (response.items.length > 0) {
        response.items.forEach((item: any) => {
          let field = {} as any
          field.name = item.metadata.name
          field.namespace = item.metadata.namespace
          let endpoints = [] as any[]
          if (item.subsets && item.subsets.length > 0) {
            item.subsets.forEach((subset) => {
              if (subset.addresses && subset.addresses.length > 0) {
                subset.addresses.forEach((address) => {
                  if (subset.ports && subset.ports.length > 0) {
                    subset.ports.forEach((port) => {
                      endpoints.push({ ip: address.ip, port: port.port })
                    })
                  }
                })
              }
            })
          }
          field.endpoints = endpoints
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
const handleShowValue = async (event: any) => {
  if (event.column.property === 'key') {
    configData.value = event.row.data
    drawConfig.value.title = event.row.name
    drawConfig.value.visible = true
  }
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
