<template>
  <div class="container-list">
    <Breadcrumb
      :items="[
        'menu.kubernetes',
        'menu.kubernetes.workloads',
        'menu.kubernetes.workloads.statefulsets'
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
          width="120"
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
          field="createAt"
          :title="$t('kubernetes.age')"
          align="center"
          width="120"
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
          resType="resourceType"
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
  TinyOption,
  TinySelect
} from '@opentiny/vue'
import { iconDel, iconEllipsis, iconEyeopen } from '@opentiny/vue-icon'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import { useI18n } from 'vue-i18n'
import { getDayAndHours } from '@/utils/date'
import { PageUtils } from '@/utils/page'
import { useClusterStore } from '@/store/modules/cluster'
import ResourceCreate from '@/views/kubernetes/components/resource-create.vue'
import emitter from '@/utils/evnetbus'
import { nanoid } from 'nanoid'

const { t } = useI18n()
const clusterStore = useClusterStore()
const loading = ref(true)
const tableHeight = ref(500)
const tinyIconEllipsis = iconEllipsis()
const namespaces = ref([])
const selectNs = ref('')
const gridData = ref<any[]>([])
const source = ref([])

//Update Resource
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
  const resName = row.name
  if (item.itemData.name === 'view') {
    currentRow.value = toRaw(row)
    currentSource.value = source.value.filter((item: any) => (item.metadata.name = resName))[0]
    showSubPage.value = true
  }
  if (item.itemData.name === 'delete') {
    Modal.confirm({
      title: '删除确认',
      message: '确定要删除' + resourceType.value + '[' + resName + ']吗？',
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
const handleBack = () => {
  showSubPage.value = false
}
emitter.on('handleBack', handleBack)

//Create Resource
const showCreate = ref(false)
const resourceType = ref('StatefulSet')
const yaml = ref('')
const handleCreate = () => {
  showCreate.value = true
  yaml.value = `apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: web
spec:
  selector:
    matchLabels:
      app: nginx # has to match .spec.template.metadata.labels
  serviceName: "nginx"
  replicas: 3 # by default is 1
  template:
    metadata:
      labels:
        app: nginx # has to match .spec.selector.matchLabels
    spec:
      terminationGracePeriodSeconds: 10
      containers:
        - name: nginx
          image: registry.k8s.io/nginx-slim:0.8
          ports:
            - containerPort: 80
              name: web
          volumeMounts:
            - name: www
              mountPath: /usr/share/nginx/html
  volumeClaimTemplates:
    - metadata:
        name: www
      spec:
        accessModes: ["ReadWriteOnce"]
        storageClassName: "my-storage-class"
        resources:
          requests:
            storage: 1Gi`
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
    const responseString = await Kubernetes.WorkloadService.listStatefulSet(selectNs.value)
    if (responseString) {
      const response = JSON.parse(responseString)
      let fields = [] as any[]
      source.value = response.items
      if (response.items.length > 0) {
        response.items.forEach((item) => {
          let field = {} as any
          field.name = item.metadata.name
          field.namespace = item.metadata.namespace
          field.pods = item.spec.template.spec.containers.length
          field.replicas = item.spec.replicas
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
