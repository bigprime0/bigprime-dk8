<template>
  <div>
    <tiny-tree-menu
        ref="tree"
        :accordion="false"
        :data="treeDataFilter"
        :show-filter="false"
        node-key="id"
        :indent="10"
        ellipsis
        @current-change="currentChange"
        :show-title="false"
    >
      <template #default="slotScope">
        <component :is="getIcon(slotScope)"></component>
        <span class="span-font" :title="$t(slotScope.label)">{{ $t(slotScope.label) }}</span>
      </template>
    </tiny-tree-menu>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue'
import type {RouteRecordNormalized} from 'vue-router'
import Svgs from '@opentiny/vue-icon'
import {TreeMenu as tinyTreeMenu} from '@opentiny/vue'
import router from '@/router'

// 获取路由数据
const appRoute = computed(() => {
  return router
      .getRoutes()
      .find((el) => el.name === 'home') as RouteRecordNormalized
})
const treeDataFilter = JSON.parse(JSON.stringify(appRoute.value.children))

const tree = ref()

const icons = [
  {label: 'menu.test', icon: 'IconTaskCooperation'}
]
const getIcon = (data: any) => {
  let find = icons.find(f => f.label === data.label)
  if (find) {
    return Svgs[find.icon] && Svgs[find.icon]()
  }else{
    return Svgs['IconSeparate'] && Svgs['IconSeparate']()
  }
}

const currentChange = (data: any) => {
  if (!data.children || data.children.length === 0) {
    router.push({name: data.name})
  }
}
</script>

<style lang="less" scoped>
.span-font {
  font-size: 13px;
}

:deep(.tiny-svg) {
  width: 0.8em;
  height: 0.8em;
  vertical-align: middle;
  overflow: hidden;
  display: inline-block;
}
:deep(.tiny-tree-node__content:hover) {
  //background-color: var(--ti-tree-node-content-hover-bg-color) !important;
  background: #e9edfa !important;
}

:deep(.tiny-tree-menu .tiny-tree .tiny-tree-node.is-current>.tiny-tree-node__content .tree-node-body) {
  color: #526ecc;
}

:deep(.tiny-tree-menu .tiny-tree .tiny-tree-node.is-current>.tiny-tree-node__content .tiny-tree-node__content-left::before) {
  left: 10px;
  border-left: 2px solid #526ecc;
}

:deep(.tiny-tree-node__wrapper > .is-expanded > .tiny-tree-node__children
  .tiny-tree-node__wrapper .is-current .tiny-tree-node__content .tiny-tree-node__content-left:before) {
  left: 50px;
  border-left: 2px solid #526ecc;
}

:deep(.tiny-tree-node > .tiny-tree-node__content) {
  margin-left: 0 !important;
}

.tiny-tree-menu {
  width: 250px;
}


.tiny-tree-menu
.tiny-tree
.tiny-tree-node.is-current
> .tiny-tree-node__content
.tree-node-name
.tiny-svg {
  //fill: var(--ti-base-icon-color);
}
</style>
