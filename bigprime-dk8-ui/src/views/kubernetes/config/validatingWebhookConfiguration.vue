<template>
  <div class="container-list">
    <Breadcrumb
      :items="[
        'menu.kubernetes',
        'menu.kubernetes.config',
        'menu.kubernetes.config.validatingwebhookconfiguration'
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

        <tiny-grid-column field="webhooks" :title="$t('kubernetes.webhooks')">
          <template #default="data">
            <div
              v-for="item in data.row.webhooks"
              :key="item.name"
              style="padding-bottom: 2px; border-bottom: 1px solid wheat"
            >
              <p>
                <tiny-tag size="small" type="info" style="min-width: 300px"
                  >name={{ item.name }}
                </tiny-tag>
              </p>

              <p>
                <tiny-tag size="small" type="info" style="min-width: 300px"
                  >service={{ item.clientConfig.service.name }}
                </tiny-tag>
              </p>
              <p>
                <tiny-tag size="small" type="info"
                  >port={{ item.clientConfig.service.port }}
                </tiny-tag>
              </p>
              <p>
                <tiny-tag size="small" type="info"
                  >namespace={{ item.clientConfig.service.namespace }}
                </tiny-tag>
              </p>
            </div>
          </template>
        </tiny-grid-column>

        <tiny-grid-column
          field="createAt"
          :title="$t('kubernetes.age')"
          align="center"
          width="120"
        ></tiny-grid-column>

<!--        <tiny-grid-column :title="$t('common.operations')" align="center" width="120">
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

const formatTimestamp = (creationTimestamp) => {
  if (creationTimestamp) {
    const date = new Date(creationTimestamp)
    return getDayAndHours(date.toLocaleString())
  }
  return creationTimestamp
}

const loadDatas = async () => {
  try {
    loading.value = true
    const responseString = await Kubernetes.ConfigService.listValidatingWebhookConfiguration()
    if (responseString) {
      const response = JSON.parse(responseString)
      let fields = [] as any[]
      source.value = response.items
      if (response.items.length > 0) {
        response.items.forEach((item: any) => {
          let field = {} as any
          field.name = item.metadata.name
          field.webhooks = item.webhooks
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
}
</style>
