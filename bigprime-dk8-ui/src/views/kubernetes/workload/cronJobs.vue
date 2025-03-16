<template>
  <div class="container-list">
    <Breadcrumb
      :items="[
        'menu.kubernetes',
        'menu.kubernetes.workloads',
        'menu.kubernetes.workloads.cronjobs'
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
          width="120"
        ></tiny-grid-column>

        <tiny-grid-column
          field="completions"
          :title="$t('kubernetes.completions')"
          align="center"
          width="130"
        ></tiny-grid-column>

        <tiny-grid-column
          field="conditions"
          :title="$t('kubernetes.conditions')"
          align="center"
          width="150"
        >
          <template #default="data">
            <span v-if="data.row.conditions == 'Complete'" style="color: green">{{
              data.row.conditions
            }}</span>
            <span v-else>{{ data.row.conditions }}</span>
          </template>
        </tiny-grid-column>

        <tiny-grid-column
          field="createAt"
          :title="$t('kubernetes.age')"
          align="center"
          width="120"
        ></tiny-grid-column>
        <!--        <tiny-grid-column :title="$t('common.operations')" align="center" width="220">
                  <template v-slot="data">
                    <tiny-action-menu
                      class="custom-icon"
                      :options="operations"
                      :suffix-icon="tinyIconEllipsis"
                      @item-click="rowBtnClick(data.row, $event)"
                      spacing="3px"
                      :max-show-num="3"
                    >
                    </tiny-action-menu>
                  </template>
                </tiny-grid-column>-->
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
  }
}
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
    const responseString = await Kubernetes.WorkloadService.listCronJob(selectNs.value)
    if (responseString) {
      const response = JSON.parse(responseString)
      let fields = [] as any[]
      source.value = response.items
      if (response.items.length > 0) {
        response.items.forEach((item) => {
          let field = {} as any
          let replicaSetNme = item.metadata.name
          field.name = replicaSetNme
          field.namespace = item.metadata.namespace
          field.completions = item.spec.completions
          if (item.status.conditions.find((e) => e.type === 'Complete').status == 'True') {
            field.conditions = 'Complete'
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
