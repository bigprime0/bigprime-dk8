<template>
  <div class="container-list">
    <div class="contain">
      <EmptyComponents :msg="msg"></EmptyComponents>
    </div>
  </div>
</template>

<script setup lang="ts">
import EmptyComponents from "@/views/components/empty-components.vue"
const msg = defineModel('msg')
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
  margin: 8px 0;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 0 8px 8px rgba(169, 174, 184, 0.05);
  padding: 10px;
  height: calc(70vh - 100px);
}
</style>