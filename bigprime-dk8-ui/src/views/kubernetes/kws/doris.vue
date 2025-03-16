<template>
  <div class="container-list">
    <Breadcrumb :items="['menu.kubernetes', 'menu.kubernetes.app', 'menu.kubernetes.app.flink']" />
    <div class="contain" ref="contain">
      <ReadmeContent></ReadmeContent>
    </div>
  </div>
</template>

<script setup lang="ts">
import ReadmeContent from '@/views/md/k8s-learn.md'
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

  //.markdown-container {
  //  margin: 20px;
  //}
}
</style>
