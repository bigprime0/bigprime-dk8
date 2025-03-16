<template>
  <div class="container-list">
    <Breadcrumb
      :items="[
        'menu.kubernetes',
        'menu.kubernetes.workloads',
        'menu.kubernetes.workloads.deployments'
      ]"
      :title="clusterStore.clusterInfo.clusterName"
    />
    <div class="contain">
      <tiny-grid
        v-if="!showSubPage"
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
              <tiny-button size="small" @click="handleCreate">创建</tiny-button>
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
          field="pods"
          :title="$t('kubernetes.pods')"
          align="center"
          width="130"
        ></tiny-grid-column>

        <tiny-grid-column
          field="replicas"
          :title="$t('kubernetes.replicas')"
          align="center"
          width="130"
        ></tiny-grid-column>

        <tiny-grid-column
          field="status"
          :title="$t('common.operations.status')"
          align="center"
          width="150"
        >
          <template #default="data">
            <span v-if="data.row.status == 'Running'" style="color: green">{{
              data.row.status
            }}</span>
            <span v-else-if="data.row.status == 'UnAvailable'" style="color: orangered">{{
              data.row.status
            }}</span>
            <span v-else-if="data.row.status == 'UnProgressing'" style="color: orange">{{
              data.row.status
            }}</span>
            <span v-else>{{ data.row.status }}</span>
          </template>
        </tiny-grid-column>

        <tiny-grid-column field="createAt" :title="$t('kubernetes.age')" align="center" width="120">
        </tiny-grid-column>

        <tiny-grid-column :title="$t('common.operations')" align="center" width="80">
          <template v-slot="data">
            <tiny-action-menu
              class="custom-icon"
              :options="operations"
              :suffix-icon="tinyIconEllipsis"
              @item-click="rowBtnClick(data.row, $event)"
              spacing="3px"
              :max-show-num="2"
            >
            </tiny-action-menu>
          </template>
        </tiny-grid-column>
      </tiny-grid>
      <div v-if="showSubPage">
        <component
          :is="subComponent"
          @back="handleBack"
          :currentRow="currentRow"
          :resType="resourceType"
          :descr="currentSource"
          :showLog="false"
        ></component>
      </div>
      <div>
        <resource-create
          :key="nanoid(8)"
          :visible="showCreate"
          :resourceType="resourceType"
          :yaml="yaml"
        ></resource-create>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, toRaw } from 'vue'
import {
  Grid as TinyGrid,
  Modal,
  TinyActionMenu,
  TinyButton,
  TinyGridColumn,
  TinyGridToolbar,
  TinyNotify,
  TinyOption,
  TinySelect
} from '@opentiny/vue'
import { iconDel, iconEllipsis, iconEyeopen } from '@opentiny/vue-icon'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import { useI18n } from 'vue-i18n'
import { getDayAndHours } from '@/utils/date'
import { PageUtils } from '@/utils/page'
import { useClusterStore } from '@/store/modules/cluster'
import { nanoid } from 'nanoid'
import ResourceCreate from '@/views/kubernetes/components/resource-create.vue'
import emitter from '@/utils/evnetbus'

const { t } = useI18n()
const clusterStore = useClusterStore()
const tableHeight = ref(500)
const tinyIconEllipsis = iconEllipsis()
const loading = ref(true)
const namespaces = ref([])
const selectNs = ref('')
const gridData = ref<any[]>([])
const source = ref([])
const currentSource = ref()
const subComponent = defineAsyncComponent(
  () => import('@/views/kubernetes/components/resource-operator.vue')
)
const showSubPage = ref(false)
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
const currentRow = ref()
const rowBtnClick = (row, item) => {
  const deployment = row.name
  if (item.itemData.name === 'view') {
    currentRow.value = toRaw(row)
    currentSource.value = source.value.filter((item: any) => (item.metadata.name = deployment))[0]
    showSubPage.value = true
  }
  if (item.itemData.name === 'delete') {
    Modal.confirm({
      title: '删除确认',
      message: '确定要删除' + resourceType.value + '[' + deployment + ']吗？',
      showHeader: true,
      showFooter: true,
      resize: true,
      events: {
        confirm(ev: any) {
          /*Kubernetes.WorkloadService.deletePod(selectNs.value, pod).then((response) => {
            if (response.startsWith('Failure')) {
              TinyNotify({ type: 'error', message: response, duration: 2000 })
            } else {
              TinyNotify({ type: 'success', message: 'Pod删除成功!', duration: 2000 })
            }
          })*/
        }
      }
    })
  }
}

//Create Resource
const showCreate = ref(false)
const resourceType = ref('Deployment')
const yaml = ref('')
const handleCreate = () => {
  showCreate.value = true
  yaml.value = `apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  namespace: ownerNS
  labels:
    app: nginx
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
        - name: nginx
          image: nginx:1.14.2
          ports:
            - containerPort: 80`
}
const handleCreateClose = () => {
  showCreate.value = false
}
emitter.on('handleCreateClose', handleCreateClose)

// 组件挂载时加载数据
onMounted(() => {
  loadNamespaces()
  loadDatas()
  tableHeight.value = PageUtils.setTableHeight(null)
})

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
    const responseString = await Kubernetes.WorkloadService.listDeployment(selectNs.value)
    if (responseString) {
      const response = JSON.parse(responseString)
      let fields = [] as any[]
      source.value = response.items
      if (response.items.length > 0) {
        response.items.forEach((item) => {
          let field = {} as any
          field.name = item.metadata.name
          field.createAt = formatTimestamp(item.metadata.creationTimestamp)
          field.pods = item.spec.template.spec.containers.length
          field.namespace = item.metadata.namespace
          if (item.status.availableReplicas === item.status.readyReplicas) {
            if (
              item.status.conditions.find((e) => e.type === 'Available').status == 'True' &&
              item.status.conditions.find((e) => e.type === 'Progressing').status == 'True'
            ) {
              field.status = 'Running'
            } else {
              if (item.status.conditions.find((e) => e.type === 'Available').status == 'False') {
                field.status = 'UnAvailable'
              } else if (
                item.status.conditions.find((e) => e.type === 'Progressing').status == 'False'
              ) {
                field.status = 'UnProgressing'
              }
            }
          } else {
            field.status = 'UnReady'
          }
          field.replicas = item.spec.replicas
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
    TinyNotify({ type: 'error', message: error, duration: 3000 })
  }
}

const handleRefresh = async () => {
  await loadDatas()
}
emitter.on('handleRefresh', handleRefresh)

const handleBack = () => {
  showSubPage.value = false
}
emitter.on('handleBack', handleBack)
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
