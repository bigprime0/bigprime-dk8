<template>
  <div class="layout">
    <tiny-container :aside-width="250">
      <template #header>
        <tiny-layout>
          <div class="layout-navbar">
            <NavBar/>
          </div>
        </tiny-layout>
      </template>
      <template #aside>
        <tiny-layout class="layout-sider">
          <div class="menu-wrapper">
            <Menu/>
          </div>
        </tiny-layout>
      </template>
      <tiny-layout class="layout-content">
        <PageLayout/>
      </tiny-layout>
    </tiny-container>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue';
import {Container as TinyContainer, Layout as TinyLayout,} from '@opentiny/vue';
import NavBar from '@/components/navbar/index.vue';
import Menu from '@/components/menu/index.vue';
import PageLayout from './page-layout.vue';

// 切换简约模式，图标按钮
const top = ref('10px');


</script>

<style scoped lang="less">
.layout {
  width: 100%;
  height: 100%;
}

.layout-navbar {
  position: fixed;
  left: 0;
  z-index: 999;
  width: 100%;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 4px 6px 0 rgba(0, 0, 0, 0.2);
}

.menu-wrapper {
  width: inherit;
  height: 92vh;
  margin-top: v-bind(top);
  overflow-x: hidden;
  overflow-y: auto;
}

.layout :deep(.tiny-container .tiny-container__aside) {
  z-index: 100;
  background: #fff;
  border-left: 1px solid #ccc;
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

.layout :deep(.tiny-container .tiny-container__main) {
  //color: #ccc;
  background-color: #f5f6f7;
}

.layout :deep(.layout-content) {
  height: 100%;
  padding: 0 10px;
  overflow: hidden;
}

.layout :deep(.tiny-container .tiny-container__footer) {
  display: none;
  padding-top: 15px;
  justify-content: center;
  background-color: #f5f6f7;
}

// 组件无法固定非message的modal类型距离顶部距离
:deep(.tiny-modal__box) {
  top: 8px !important;
}

// 路由子菜单选中后的样式
:deep(.tiny-tree-node__children .tiny-tree-node__content) {
  .tree-node-name {
    margin-left: 28px !important;
    padding-left: 6px !important;
  }
}

:deep(.tiny-tree-node__children > .tree-node-body) {
  padding-left: 50px;
}

.theme-box {
  position: fixed;
  top: 88%;
  right: 30px;
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background-color: #fff;
  border-radius: 100%;
  cursor: pointer;

  img {
    display: block;
    width: inherit;
    height: inherit;
  }
}

.layout :deep(.tiny-button.tiny-button--primary) {
  //color: var(--ti-button-primary-text-color);
  //fill: var(--ti-button-primary-text-color);
  border-color: #002FA7;
  background-color: #002FA7;
}

.layout :deep(.tiny-button.tiny-button--primary.is-plain) {
  //color: var(--ti-button-primary-plain-text-color);
  //fill: var(--ti-button-primary-plain-text-color);
  border-color: var(--ti-button-primary-plain-border-color);
  background-color: var(--ti-button-primary-plain-bg-color);
}

.layout :deep(.tiny-split-pane.left-pane, .tiny-split-pane.right-pane) {
  top: 0;
  bottom: 0;
  overflow: hidden;
}

.layout :deep {
  button svg,
  button polygon,
  button path,
  span.ql-picker-label svg,
  span.ql-picker-label polygon,
  span.ql-picker-label path {
    fill: #fff;
  }
}
</style>

<style>
.popover-cls {

}

.popover-cls .tiny-popover__title {
  font-size: 12px !important;
}
</style>
