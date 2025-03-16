<template>
  <div class="container-list">
    <Breadcrumb
      :items="['menu.kubernetes', 'menu.kubernetes.namespace']"
      :title="clusterStore.clusterInfo.clusterName"
    />
    <div class="contain">
      <tiny-grid
        :data="namespaces"
        :fit="true"
        size="small"
        border
        :stripe="true"
        show-header-overflow="tooltip"
        show-overflow="tooltip"
        highlight-hover-row
        :height="tableHeight"
        @toolbar-button-click="handleToolbarClick"
        :loading="loading"
      >
        <template #toolbar>
          <tiny-grid-toolbar
            class="search-toolbar"
            :buttons="toolbarButtons"
            size="small"
            setting
            full-screen
          >
          </tiny-grid-toolbar>
        </template>

        <tiny-grid-column type="index" width="50"></tiny-grid-column>

        <tiny-grid-column
          field="metadata.name"
          :title="$t('common.name')"
          width="400"
        ></tiny-grid-column>

        <tiny-grid-column field="labels" :title="$t('kubernetes.labels')">
          <template #default="data">
            <template v-for="(value, key) in data.row.metadata.labels" :key="key">
              <tiny-tag effect="light" size="small" style="max-width: 500px"
                >{{ key }}:{{ value }}
              </tiny-tag>
            </template>
          </template>
        </tiny-grid-column>

        <tiny-grid-column
          field="status.phase"
          :title="$t('common.operations.status')"
          width="150"
          align="center"
        >
          <template #default="data">
            <tiny-tag type="success" v-if="data.row.status.phase == 'Active'" effect="dark"
              >{{ data.row.status.phase }}
            </tiny-tag>
            <tiny-tag v-else effect="dark">{{ data.row.status.phase }}</tiny-tag>
          </template>
        </tiny-grid-column>

        <tiny-grid-column
          field="metadata.creationTimestamp"
          :title="$t('kubernetes.age')"
          width="120"
          align="center"
        >
          <template #default="data">
            <span>{{ formatTimestamp(data.row) }}</span>
          </template>
        </tiny-grid-column>

        <tiny-grid-column :title="$t('common.operations')" width="80" align="center">
          <template #default="data">
            <tiny-action-menu
              class="custom-icon"
              :options="operations"
              :suffix-icon="iconEdit"
              @item-click="rowBtnClick(data.row, $event)"
              spacing="3px"
              :max-show-num="2"
            >
            </tiny-action-menu>
          </template>
        </tiny-grid-column>
      </tiny-grid>
    </div>
    <div>
      <resource-create
        :key="nanoid(8)"
        :visible="showCreate"
        :resourceType="resourceType"
        :yaml="yaml"
      ></resource-create>
    </div>
    <div>
      <resource-view
        :key="nanoid(8)"
        :visible="showView"
        :resourceType="resourceType"
        :content="viewDetail"
      ></resource-view>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import {
  Modal,
  TinyActionMenu,
  TinyGrid,
  TinyGridColumn,
  TinyGridToolbar,
  TinyNotify,
  TinyTag
} from '@opentiny/vue'
import { useI18n } from 'vue-i18n'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import { getDayAndHours } from '@/utils/date'
import { PageUtils } from '@/utils/page'
import { useClusterStore } from '@/store/modules/cluster'
import { iconDel, iconEdit, iconEyeopen } from '@opentiny/vue-icon'
import { nanoid } from 'nanoid'
import ResourceCreate from '@/views/kubernetes/components/resource-create.vue'
import ResourceView from '@/views/kubernetes/components/resource-view.vue'
import emitter from '@/utils/evnetbus'

const { t } = useI18n()
const loading = ref(false)
const clusterStore = useClusterStore()
// 表格数据
const namespaces = ref([])
const tableHeight = ref(500)
const formatTimestamp = (data) => {
  if (data.metadata?.creationTimestamp) {
    const timestampInMilliseconds = data.metadata.creationTimestamp * 1000
    const date = new Date(timestampInMilliseconds)
    return getDayAndHours(date.toLocaleString())
  }
  return data
}

// 组件挂载时加载数据
onMounted(() => {
  loadNamespaces()
  tableHeight.value = PageUtils.setTableHeight(null)
})

//工具栏操作
const toolbarButtons = ref([
  {
    code: 'add',
    name: t('common.operations.create')
  },
  {
    code: 'refresh',
    name: t('common.operations.refresh')
  }
])
const handleToolbarClick = ({ code, $grid }) => {
  switch (code) {
    case 'add':
      handleCreate()
      break
    case 'refresh':
      loadNamespaces()
      break
  }
}

const operations = ref([
  {
    name: 'view',
    icon: iconEyeopen()
  },
  {
    name: 'delete',
    icon: iconDel()
  }
])

// 加载命名空间数据
const loadNamespaces = async () => {
  try {
    loading.value = true
    const response = await Kubernetes.NamespaceService.listNamespace()
    namespaces.value = response.items
    loading.value = false
  } catch (error) {
    loading.value = false
    TinyNotify({ type: 'error', message: error, duration: 3000 })
  }
}

//View Resource
const showView = ref(false)
const viewDetail = ref('')
emitter.on('handleViewClose', () => {
  showView.value = false
})

const rowBtnClick = (row, item) => {
  const ns = row.metadata.name
  switch (item.itemData.name) {
    case 'view':
      viewDetail.value = namespaces.value.filter((item) => item.metadata.name === ns)[0]
      showView.value = true
      break
    case 'delete':
      Modal.confirm({
        title: '删除确认',
        message: '确定要删除Namespace[' + ns + ']吗？',
        showHeader: true,
        showFooter: true,
        resize: true,
        events: {
          confirm(ev) {
            Kubernetes.ResourceService.resourceDelete({
              resType: 'Namespace',
              resName: ns,
              namespace: ''
            }).then((response) => {
              if (response.startsWith('Failure')) {
                TinyNotify({
                  type: 'error',
                  message: response,
                  position: 'top-right',
                  duration: 5000
                })
              } else {
                TinyNotify({
                  type: 'success',
                  message: 'Namespace删除成功!',
                  position: 'top-right',
                  duration: 5000,
                  beforeClose: () => {
                    loadNamespaces()
                  }
                })
              }
            })
          }
        }
      })
      break
  }
}

//Create Resource
const showCreate = ref(false)
const resourceType = ref('Namespace')
const yaml = ref('')
const handleCreate = () => {
  showCreate.value = true
  yaml.value = `apiVersion: v1
kind: Namespace
metadata:
  name: test
  labels:
    labelKey: labelValue `
}
emitter.on('handleCreateClose', () => {
  showCreate.value = false
})

const handleRefresh = async () => {
  await loadNamespaces()
}
emitter.on('handleRefresh', handleRefresh)
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

  .step-button {
    margin: 0;
    text-align: center;
    padding-top: 20px;
  }
}

.tags-container {
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap; /* 允许换行 */
  gap: 4px; /* 标签之间的间距 */
}

.tag-item {
  flex: 1 1 auto; /* 自适应宽度 */
}
</style>
