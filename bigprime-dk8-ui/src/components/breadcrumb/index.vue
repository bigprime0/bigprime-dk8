<template>
  <tiny-breadcrumb class="container-breadcrumb" :separator-icon="IconSeparator">
    <IconLeft style="margin: 1px 5px 3px 0"></IconLeft>
    <tiny-tag
      type="success"
      v-if="title != ''"
      style="margin-top: -4px; margin-right: 3px; font-weight: 600"
    >
      [ {{ title }} ]
    </tiny-tag>
    <tiny-breadcrumb-item v-for="item in items" :key="item">
      {{ $t(item) }}
    </tiny-breadcrumb-item>
  </tiny-breadcrumb>
</template>

<script lang="ts" setup>
import {
  Breadcrumb as TinyBreadcrumb,
  BreadcrumbItem as TinyBreadcrumbItem,
  TinyTag
} from '@opentiny/vue'
import { iconChevronRight, iconEditorMenuRight } from '@opentiny/vue-icon'

const IconLeft = defineModel('IconLeft', { default: iconEditorMenuRight() })
const IconSeparator = defineModel('IconSeparator', { default: iconChevronRight() })
const items = defineModel('items', { default: [] })
const title = defineModel('title', { default: '' })
</script>

<style scoped lang="less">
.container-breadcrumb {
  margin: 15px 0px 5px 10px;
  font-size: 13px;

  :deep(.tiny-svg) {
    width: 1.1em;
    height: 1.1em;
    vertical-align: middle;
    overflow: hidden;
    display: inline-block;
  }

  :deep(.tiny-breadcrumb .tiny-breadcrumb__separator) {
    font-family: var(--ti-breadcrumb-font-family);
    color: var(--ti-breadcrumb-separator-text-color);
    margin: 0 3px 0 3px;
    vertical-align: middle;
    text-align: center;
  }
}
</style>
