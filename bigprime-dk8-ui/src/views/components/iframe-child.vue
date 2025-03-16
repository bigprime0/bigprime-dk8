<template>
  <component :is="currentComponent" :parent-params="parentParams" :msg="msg"></component>
</template>

<script setup lang="ts">
import {defineAsyncComponent, onMounted, onUnmounted, ref} from 'vue'
import {isEmpty} from "lodash-es"
import {getToken, removeToken, setToken} from "@/utils/token"
import useLocale from "@/hooks/locale"
import useUserStore from "../../store/modules/user"

const {changeLocale} = useLocale()
const userStore = useUserStore()
const currentComponent = ref<any>(null)
const errorComponent = ref('./iframe-error.vue')
const parentParams = ref<any>(null)
const msg = ref('')
const loadComponent = async (event: any) => {
  let error = false
  if (event.data.key === 'bigprime-iframe') {
    if (!isEmpty(event.data.token)) {
      if(event.data.token !== getToken()){
        setToken(event.data.token, "-1", true, {})
        const res = await userStore.checkToken(event.data.token)
        if(!res){
          error = true
          msg.value = '无效的用户信息'
          removeToken()
        }
      }
    }else{
      error = true
      msg.value = '无效的访问信息'
    }

    if (!error && !isEmpty(event.data.lange)) {
      changeLocale(event.data.lange)
    }

    if (!error && !isEmpty(event.data.componentPath)) {
      try {
        parentParams.value = event.data.data
        currentComponent.value = await defineAsyncComponent(() => import(event.data.componentPath));
      } catch (er) {
        error = true
        msg.value = '无效的访问地址'
      }
    }
  }
  if (error) {
    currentComponent.value = await defineAsyncComponent(() => import(errorComponent.value));
  }
}

onMounted(async () => {
  window.addEventListener('message', loadComponent);
})

onUnmounted(() => {
  window.removeEventListener('message', loadComponent);
})
</script>

<style scoped lang="less">
</style>
