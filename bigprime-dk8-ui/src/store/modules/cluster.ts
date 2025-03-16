import { defineStore } from 'pinia'
import type { ClusterModel } from '@/services/kubernetes/kubernetes'

export const useClusterStore = defineStore('cluster', {
  state: () => {
    return {
      clusterInfo: {} as ClusterModel
    }
  },

  actions: {
    setDefault(clusterModel: ClusterModel) {
      this.$reset()
      this.$patch({ clusterInfo: clusterModel })
      sessionStorage.removeItem('CurrentCluster')
      sessionStorage.setItem('CurrentCluster', JSON.stringify(clusterModel))
    }
  }
})
