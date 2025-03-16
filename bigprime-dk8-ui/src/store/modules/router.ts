import {defineStore} from 'pinia'
import type {RouteRecordRaw} from 'vue-router'

export interface RouterDes {
    id: number,
    pid: number,
    name: string,
    url: string,
    icon: string,
    urlDes: string,
    pathDes: string,
    nameDes: string
}

export interface MenuSelect {
    id: number,
    label: string,
    children: MenuSelect[]
}

const allModules = import.meta.glob('/src/views/**/*.vue')
const getComponent = (url: string) => {
    const component = allModules[`/src/views/${url}.vue`]
    return component
}

export const useRouterStore = defineStore('myRouter', {
    state: () => ({
        routers: [] as RouteRecordRaw[],
        routerDes: [] as RouterDes[],
        filter: [] as string[],
        menuSelects: [] as MenuSelect[]
    }),

    actions: {
        resetInfo() {
            this.$reset()
        }
    }
})

