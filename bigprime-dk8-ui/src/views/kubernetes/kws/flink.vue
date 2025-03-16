<template>
  <div class="container-list">
    <Breadcrumb :items="['menu.kubernetes', 'menu.kubernetes.app', 'menu.kubernetes.app.flink']" />
    <div class="contain" ref="contain">
      <MarkdownRender :source="content" class="markdown-container" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import MarkdownRender from '@/views/components/markdown-render.vue'

const content = ref('')
onMounted(() => {
  content.value =
    '## 二、安装ikuai路由器\n' +
    '### 1、虚拟机配置 [iKuai8_x64_3.7.15_Build202409251708.iso](软件包\\iKuai8_x64_3.7.15_Build202409251708.iso) \n' +
    '- 系统选择为其它\n' +
    '- 内存1g；处理器1核；磁盘8g\n' +
    '- 网卡配置两块：1块为仅主机与其它主机相通，1块为NAT以提供外网访问\n' +
    '- 安装完成后，设置LAN: 192.168.66.200/255.255.255.0\n' +
    '- 设置完成后 在浏览器输入 http://192.168.66.200进入ikuai的管理控制台，默认用户名密码：admin/admin\n' +
    '- 配置WAN外网绑定网卡'
})
</script>

<style lang="less">
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
