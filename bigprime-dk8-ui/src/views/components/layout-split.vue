<template>
  <div class="container-list">
    <slot name="breadcrumb"></slot>
    <div class="contain">
      <div class="split-v-model">
        <tiny-split v-model="ratio" trigger-simple disabled>
          <template #left>
            <slot name="split-left"></slot>
          </template>
          <template #right>
            <slot name="split-right"></slot>
          </template>
        </tiny-split>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {Split as TinySplit} from '@opentiny/vue'

const ratio = defineModel('ratio', {default: 0.2})
</script>

<style scoped lang="less">

.container-list {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  overflow: auto;
}

.contain {
  flex: 1 1 auto;
  margin: 8px 10px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 0 8px 8px rgba(169, 174, 184, 0.05);
  padding: 10px;
}

.split-v-model {
  height: calc(85vh - 100px);
  border: 1px solid #d9d9d9;

  :deep(.tiny-split-pane.left-pane, .tiny-split-pane.right-pane) {
    top: 0;
    bottom: 0;
    overflow: auto;
  }
}
</style>
