<template>
  <div class="container-list">
    <Breadcrumb
      :items="['menu.kubernetes', 'menu.kubernetes.workloads', 'menu.kubernetes.workloads.pods']"
      :title="clusterStore.clusterInfo.clusterName"
    />
    <div class="contain" ref="contain">
      <tiny-grid
        ref="gridRef"
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
        v-if="!showSubPage"
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

        <tiny-grid-column type="index" width="50" fixed="left"></tiny-grid-column>

        <tiny-grid-column
          field="name"
          :title="$t('common.name')"
          min-width="300"
          fixed="left"
        ></tiny-grid-column>

        <tiny-grid-column
          field="namespace"
          :title="$t('kubernetes.namespace')"
          width="150"
          show-overflow
        ></tiny-grid-column>

        <tiny-grid-column
          field="containers"
          :title="$t('kubernetes.containers')"
          align="center"
          width="90"
        ></tiny-grid-column>

        <tiny-grid-column
          field="cpu"
          :title="$t('kubernetes.cpu')"
          align="center"
          width="90"
        ></tiny-grid-column>

        <tiny-grid-column
          field="memory"
          :title="$t('kubernetes.memory')"
          align="center"
          width="100"
        ></tiny-grid-column>

        <tiny-grid-column
          field="restart"
          :title="$t('kubernetes.restart')"
          align="center"
          width="90"
        ></tiny-grid-column>

        <tiny-grid-column
          field="controlledBy"
          :title="$t('kubernetes.controlledBy')"
          align="center"
          width="140"
        ></tiny-grid-column>

        <tiny-grid-column
          field="node"
          :title="$t('kubernetes.node')"
          align="center"
          width="130"
          show-overflow
        ></tiny-grid-column>

        <tiny-grid-column
          field="qos"
          :title="$t('kubernetes.qos')"
          align="center"
          width="130"
        ></tiny-grid-column>

        <tiny-grid-column
          field="status"
          :title="$t('common.operations.status')"
          align="center"
          width="90"
        >
          <template #default="data">
            <span v-if="data.row.status == 'Running'" style="color: green">{{
              data.row.status
            }}</span>
            <span v-else-if="data.row.status == 'Pending'" style="color: orangered">{{
              data.row.status
            }}</span>
            <span v-else>{{ data.row.status }}</span>
          </template>
        </tiny-grid-column>

        <tiny-grid-column
          field="createAt"
          :title="$t('kubernetes.age')"
          align="center"
          width="90"
        ></tiny-grid-column>

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
          resType="Pod"
          :descr="currentSource"
          :showLog="true"
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
  TinyOption,
  TinySelect
} from '@opentiny/vue'
import { iconDel, iconEllipsis, iconEyeopen } from '@opentiny/vue-icon'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import { useI18n } from 'vue-i18n'
import { getDayAndHours } from '@/utils/date'
import { PageUtils } from '@/utils/page'
import { useClusterStore } from '@/store/modules/cluster'
import emitter from '@/utils/evnetbus'
import ResourceCreate from '@/views/kubernetes/components/resource-create.vue'
import { nanoid } from 'nanoid'

const { t } = useI18n()
const clusterStore = useClusterStore()
const loading = ref(false)
const tableHeight = ref(500)
const tinyIconEllipsis = iconEllipsis()
const namespaces = ref([])
const selectNs = ref('')
const gridRef = ref(null)
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
const rowBtnClick = (row: any, item: any) => {
  const pod = row.name
  if (item.itemData.name === 'view') {
    currentRow.value = toRaw(row)
    currentSource.value = source.value.filter((item: any) => item.metadata.name == pod)[0]
    showSubPage.value = true
  }
  if (item.itemData.name === 'delete') {
    Modal.confirm({
      title: '删除确认',
      message: '确定要删除' + resourceType.value + '[' + pod + ']吗？',
      showHeader: true,
      showFooter: true,
      resize: true,
      events: {
        confirm(ev: any) {
          Kubernetes.ResourceService.resourceDelete({
            resType: resourceType.value,
            resName: pod,
            namespace: selectNs.value
          }).then((response) => {
            if (response.startsWith('Failure')) {
              Modal.message({ status: 'error', message: response, top: 20 })
            } else {
              Modal.message({ status: 'error', message: 'Pod删除成功!', top: 20 })
            }
          })
        }
      }
    })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadNamespaces()
  loadDatas()
  tableHeight.value = PageUtils.setTableHeight(null)
})

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
  namespaces.value = ns.items.map((n) => {
    return n.metadata.name
  })
}

const loadDatas = async () => {
  try {
    loading.value = true
    const responseString = await Kubernetes.WorkloadService.listPod(selectNs.value)
    const metrics = await Kubernetes.WorkloadService.getPodMetrics(selectNs.value)
    if (responseString) {
      const response = JSON.parse(responseString)
      let fields = [] as any[]
      source.value = response.items
      if (response.items.length > 0) {
        response.items.forEach((item: any) => {
          let field = {} as any
          let podName = item.metadata.name
          field.name = podName
          field.createAt = formatTimestamp(item.metadata.creationTimestamp)
          field.namespace = item.metadata.namespace
          field.containers = item.spec.containers.length
          if (metrics.length > 0) {
            const find = metrics.find((m) => m.podName == podName)
            if (find) {
              if (find.containerMetrics && find.containerMetrics.length > 0) {
                let memoryCount = 0
                let cpuCount = 0
                find.containerMetrics.forEach((metric) => {
                  cpuCount += metric.cpu
                  memoryCount += metric.memory
                })
                field.cpu = Number(cpuCount.toFixed(4))
                field.memory = Number((memoryCount / (1024.0 * 1024.0)).toFixed(2)) + 'MB'
              }
            } else {
              field.cpu = 0
              field.memory = 0 + 'MB'
            }
          }
          if (item.status.containerStatuses) {
            field.restart = item.status.containerStatuses[0].restartCount
          }
          if (item.metadata.ownerReferences) {
            field.controlledBy = item.metadata.ownerReferences[0].kind
          }
          field.node = item.spec.nodeName
          field.qos = item.status.qosClass
          field.status = item.status.phase
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

//Create Resource
const showCreate = ref(false)
const resourceType = ref('Pod')
const yaml = ref('')
const handleCreate = () => {
  showCreate.value = true
  yaml.value = `apiVersion: v1
kind: Pod
metadata:
  name: test111
  namespace: default
  labels:
    key: value
spec:
  containers:
    - name: web
      image: nginx
      imagePullPolicy: IfNotPresent
      ports:
        - name: web
          containerPort: 80
          protocol: TCP`
}

const handleCreateClose = () => {
  showCreate.value = false
}
emitter.on('handleCreateClose', handleCreateClose)

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
