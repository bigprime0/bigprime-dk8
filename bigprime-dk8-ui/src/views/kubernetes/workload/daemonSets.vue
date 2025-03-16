<template>
  <div class="container-list">
    <Breadcrumb
      :items="[
        'menu.kubernetes',
        'menu.kubernetes.workloads',
        'menu.kubernetes.workloads.daemonSets'
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

        <tiny-grid-column field="name" :title="$t('common.name')" width="200"></tiny-grid-column>

        <tiny-grid-column
          field="namespace"
          :title="$t('kubernetes.namespace')"
          width="150"
        ></tiny-grid-column>

        <tiny-grid-column
          field="desired"
          :title="$t('kubernetes.daemonSet.desired')"
          align="center"
          width="110"
        ></tiny-grid-column>

        <tiny-grid-column
          field="current"
          :title="$t('kubernetes.daemonSet.current')"
          align="center"
          width="110"
        ></tiny-grid-column>

        <tiny-grid-column
          field="updated"
          :title="$t('kubernetes.daemonSet.updated')"
          align="center"
          width="110"
        ></tiny-grid-column>

        <tiny-grid-column
          field="ready"
          :title="$t('kubernetes.daemonSet.ready')"
          align="center"
          width="110"
        ></tiny-grid-column>

        <tiny-grid-column
          field="available"
          :title="$t('kubernetes.daemonSet.available')"
          align="center"
          width="120"
        ></tiny-grid-column>

        <tiny-grid-column
          field="nodeSelector"
          :title="$t('kubernetes.daemonSet.nodeSelector')"
          show-overflow
        >
          <template #default="data">
            <div v-for="(value, key) in data.row.nodeSelector" :key="key">
              <tiny-tag size="small" type="info" style="min-width: 160px"
                >{{ key }}={{ value }}
              </tiny-tag>
            </div>
          </template>
        </tiny-grid-column>

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
  TinySelect,
  TinyTag
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
const loading = ref(true)
const clusterStore = useClusterStore()
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

//Create Resource
const showCreate = ref(false)
const resourceType = ref('DaemonSet')
const yaml = ref('')
const handleCreate = () => {
  showCreate.value = true
  yaml.value = `apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: fluentd-elasticsearch
  namespace: kube-system
  labels:
    k8s-app: fluentd-logging
spec:
  selector:
    matchLabels:
      name: fluentd-elasticsearch
  template:
    metadata:
      labels:
        name: fluentd-elasticsearch
    spec:
      tolerations:
        # this toleration is to have the daemonset runnable on master nodes
        # remove it if your masters can't run pods
        - key: node-role.kubernetes.io/master
          effect: NoSchedule
      containers:
        - name: fluentd-elasticsearch
          image: quay.io/fluentd_elasticsearch/fluentd:v2.5.2
          resources:
            limits:
              memory: 200Mi
            requests:
              cpu: 100m
              memory: 200Mi
          volumeMounts:
            - name: varlog
              mountPath: /var/log
            - name: varlibdockercontainers
              mountPath: /var/lib/docker/containers
              readOnly: true
      terminationGracePeriodSeconds: 30
      volumes:
        - name: varlog
          hostPath:
            path: /var/log
        - name: varlibdockercontainers
          hostPath:
            path: /var/lib/docker/containers`
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
    const responseString = await Kubernetes.WorkloadService.listDaemonSet(selectNs.value)
    if (responseString) {
      const response = JSON.parse(responseString)
      let fields = [] as any[]
      source.value = response.items
      if (response.items.length > 0) {
        response.items.forEach((item: any) => {
          let field = {} as any
          field.name = item.metadata.name
          field.namespace = item.metadata.namespace
          field.desired = item.status.desiredNumberScheduled
          field.current = item.status.currentNumberScheduled
          field.ready = item.status.numberReady
          field.updated = item.status.updatedNumberScheduled
          field.available = item.status.numberAvailable
          if (item.spec.template) {
            if (item.spec.template.spec.nodeSelector) {
              field.nodeSelector = item.spec.template.spec.nodeSelector
            }
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
