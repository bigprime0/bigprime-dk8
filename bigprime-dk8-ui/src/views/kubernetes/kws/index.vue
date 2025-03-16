<template>
  <div class="container-list">
    <Breadcrumb :items="['menu.kubernetes.kws']" />
    <div class="contain">
      <tiny-search v-model="searchValue" placeholder="搜索" :maxlength="10"></tiny-search>
      <div style="padding-top: 20px">
        <tiny-row class="deploy-card">
          <tiny-col :span="2">
            <div class="col">
              <tiny-card
                title="Apache Flink"
                type="logo"
                size="mini"
                :src="flink"
                @click="handleClick"
              >
              </tiny-card>
            </div>
          </tiny-col>
        </tiny-row>
      </div>
      <div class="md-container">
        <tiny-dialog-box
          :visible="visible"
          fullscreen
          @update:visible="visible = $event"
          title="Flink布署"
          :close-on-press-escape="false"
          :close-on-click-modal="false"
          :show-close="true"
          class="resource-dialog"
        >
          <ReadmeContent></ReadmeContent>
        </tiny-dialog-box>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { TinyCard, TinyCol, TinyDialogBox, TinyRow, TinySearch } from '@opentiny/vue'
import flink from '@/assets/images/Flink.png'
import ReadmeContent from '@/views/md/Flink.md'

const searchValue = ref('')
const visible = ref(false)

onMounted(() => {})

const handleClick = () => {
  visible.value = true
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

  .deploy-card {
    cursor: pointer;

    :deep(.tiny-card) {
      background: #fff;
      border-radius: 10px;
      box-shadow: 1px 1px 6px 6px #1f347914;
      padding: 2px;
    }
  }

  .md-container {
    :deep(.tiny-dialog-box .tiny-dialog-box__header) {
      color: #fff;
      background: #252b3a;
    }

    :deep(.tiny-dialog-box__body) {
      max-height: 100vh;
      overflow-y: scroll;
    }
  }
}
</style>
