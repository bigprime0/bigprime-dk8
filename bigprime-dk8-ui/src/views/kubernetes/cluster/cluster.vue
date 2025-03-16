<template>
  <div class="container-list">
    <Breadcrumb
      :items="['menu.kubernetes', 'breadcrumb.cluster']"
      :title="currentCluster.clusterName"
    />
    <div class="contain">
      <tiny-grid
        :data="gridData"
        ref="gridRef"
        :fit="true"
        size="small"
        border
        :stripe="true"
        show-header-overflow="tooltip"
        show-overflow="tooltip"
        highlight-hover-row
        :height="tableHeight"
        @toolbar-button-click="handleToolbarClick"
        v-if="!showSubPage"
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
          field="clusterId"
          :title="$t('kubernetes.cluster.id')"
          width="300"
        ></tiny-grid-column>

        <tiny-grid-column field="clusterName" :title="$t('kubernetes.cluster.name')">
          <template #default="data">
            <span v-if="data.row.clusterId == currentCluster.clusterId">
              {{ data.row.clusterName }} <tiny-tag type="success">默认</tiny-tag>
            </span>
            <span v-else>
              {{ data.row.clusterName }}
            </span>
          </template>
        </tiny-grid-column>

        <tiny-grid-column
          field="clusterNode"
          :title="$t('kubernetes.cluster.node')"
          width="150"
        ></tiny-grid-column>

        <tiny-grid-column
          field="version"
          :title="$t('kubernetes.cluster.version')"
          width="150"
        ></tiny-grid-column>

        <tiny-grid-column
          field="createTime"
          :title="$t('common.createDate')"
          align="center"
          width="250"
        >
          <template #default="data">
            <span>{{ formatTimestamp(data.row.createTime) }}</span>
          </template>
        </tiny-grid-column>

        <tiny-grid-column :title="$t('common.operations')" align="center" width="220">
          <template v-slot="data">
            <tiny-action-menu
              class="custom-icon"
              :options="operations"
              :suffix-icon="iconEdit"
              @item-click="rowBtnClick(data.row, $event)"
              spacing="3px"
              :max-show-num="3"
            >
            </tiny-action-menu>
          </template>
        </tiny-grid-column>
      </tiny-grid>
      <!-- 子页面容器 -->
      <div v-if="showSubPage">
        <component :is="subComponent" @back="handleBack" :currentRow="currentRow"></component>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, toRaw } from 'vue'
import { useI18n } from 'vue-i18n'
import { iconCreating, iconDelete, iconEdit } from '@opentiny/vue-icon'
import {
  Modal,
  TinyActionMenu,
  TinyGrid,
  TinyGridColumn,
  TinyGridToolbar,
  TinyNotify,
  TinyTag
} from '@opentiny/vue'
import emitter from '@/utils/evnetbus'
import { Kubernetes } from '@/services/kubernetes/kubernetes'
import { formatDate } from '@/utils/date'
import { useClusterStore } from '@/store/modules/cluster'
import { PageUtils } from '@/utils/page'

const subComponent = defineAsyncComponent(() => import('./cluster-form.vue'))
const showSubPage = ref(false)
const currentCluster = ref({
  clusterId: '',
  clusterName: ''
})
const clusterStore = useClusterStore()
const currentRow = ref({
  clusterId: '',
  clusterName: '',
  configContent: ''
})

function handleBack() {
  showSubPage.value = false
  loadGrid()
}

emitter.on('handleBack', handleBack)
const tableHeight = ref(500)
const { t } = useI18n()
const gridRef = ref(null)
const gridData = ref<any[]>([])
const toolbarButtons = ref([
  {
    code: 'addCluster',
    name: '新建'
  }
])

const handleToolbarClick = ({ code, $grid }) => {
  switch (code) {
    case 'addCluster':
      showSubPage.value = true
      break
  }
}

onMounted(() => {
  const sessionCluster = sessionStorage.getItem('CurrentCluster')
  if (sessionCluster) {
    currentCluster.value = JSON.parse(sessionCluster)
  }
  loadGrid()
  tableHeight.value = PageUtils.setTableHeight(null)
})

const operations = ref([
  {
    label: t('common.operations.edit'),
    name: 'edit',
    icon: iconEdit()
  },
  {
    label: t('common.operations.set.default'),
    name: 'switch',
    icon: iconCreating()
  },
  {
    label: t('common.operations.delete'),
    name: 'delete',
    icon: iconDelete()
  }
])

const rowBtnClick = (row, item) => {
  switch (item.itemData.name) {
    case 'edit':
      currentRow.value = toRaw(row)
      showSubPage.value = true
      break
    case 'switch':
      setDefaultCluster(row.clusterId, row.clusterName, true)
      break
    case 'delete':
      Modal.confirm('确定要删除集群吗?').then((res) => {
        Kubernetes.ClusterService.removeCluster(row.clusterId).then((resp) => {
          if (resp === true) {
            Modal.message({ status: 'success', message: '删除成功!', top: 20 })
            if (clusterStore.clusterInfo.clusterId === row.clusterId) {
              sessionStorage.removeItem('CurrentCluster')
              clusterStore.$reset()
            }
            loadGrid()
          }
        })
      })
      break
  }
}

const loadGrid = async () => {
  const resp = await Kubernetes.ClusterService.listCluster()
  if (resp) {
    gridData.value = resp
    if (gridData.value.length == 1) {
      const row = gridData.value[0]
      setDefaultCluster(row.clusterId, row.clusterName, false)
    }
  }
}

const formatTimestamp = (data: any) => {
  if (data) {
    const date = new Date(data)
    return formatDate(date, '')
  }
  return data
}

const setDefaultCluster = (clusterId: string, clusterName: string, isNotify: boolean) => {
  Kubernetes.ClusterService.switchCluster(clusterId).then((response) => {
    if (response) {
      currentCluster.value = {
        clusterId: clusterId,
        clusterName: clusterName
      }
      clusterStore.setDefault(toRaw(currentCluster.value))
      if (isNotify) {
        TinyNotify({
          type: 'success',
          title: '',
          message: '默认集群设置成功',
          position: 'top-right',
          duration: 2000
        })
      }
    }
  })
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

  .grid-toolbar-layout {
    display: flex;
    justify-content: space-between;
  }
}
</style>
