import request from '@/utils/request'

export interface ClusterModel {
  clusterId: string
  clusterName: string
}

export namespace Kubernetes {
  export namespace ClusterService {
    export const listCluster = async () => {
      const res = await request.get('/kubernetes/cluster/list')
      return res.data || res
    }

    export const registerCluster = async (param: {}) => {
      const res = await request.post('/kubernetes/cluster/register', param)
      return res.data || res
    }

    export const removeCluster = async (id: string) => {
      const res = await request.delete(`/kubernetes/cluster/${id}`)
      return res.data || res
    }

    export const switchCluster = async (id: string) => {
      const res = await request.get(`/kubernetes/cluster/switch/${id}`)
      return res.data || res
    }
  }

  export namespace NodeService {
    export const listNodes = async () => {
      const res = await request.get('/kubernetes/node/list')
      return res.data || res
    }

    export const getNodeMetrics = async () => {
      const res = await request.get('/kubernetes/node/metrics')
      return res.data || res
    }

    export const nodeCordon = async (name: string) => {
      const res = await request.post(`/kubernetes/node/cordon`, { nodeName: name })
      return res.data || res
    }

    export const nodeUnCordon = async (name: string) => {
      const res = await request.post(`/kubernetes/node/unCordon`, { nodeName: name })
      return res.data || res
    }

    export const nodeDrain = async (name: string) => {
      const res = await request.post(`/kubernetes/node/drain`, { nodeName: name })
      return res.data || res
    }
  }

  export namespace NamespaceService {
    export const listNamespace = async () => {
      const res = await request.get('/kubernetes/namespace/list')
      return res.data || res
    }
  }

  export namespace WorkloadService {
    export const listPod = async (name: string) => {
      const res = await request.post(`/kubernetes/pod/list`, { namespace: name })
      return res.data || res
    }

    export const getPodMetrics = async (name: string) => {
      const res = await request.post(`/kubernetes/pod/metrics`, { namespace: name })
      return res.data || res
    }

    export const getPodLogs = async (namespace: string, pod: string) => {
      const res = await request.post(`/kubernetes/pod/logs`, { namespace: namespace, pod: pod })
      return res.data || res
    }

    export const listDeployment = async (name: string) => {
      const res = await request.post(`/kubernetes/deployment/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listDaemonSet = async (name: string) => {
      const res = await request.post(`/kubernetes/daemonSet/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listStatefulSet = async (name: string) => {
      const res = await request.post(`/kubernetes/statefulSet/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listReplicaSet = async (name: string) => {
      const res = await request.post(`/kubernetes/replicaSet/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const getReplicaSetsStatus = async (name: string) => {
      const res = await request.post(`/kubernetes/replicaSet/status`, {
        namespace: name
      })
      return res.data || res
    }

    export const listReplicationController = async (name: string) => {
      const res = await request.post(`/kubernetes/replicationController/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listJob = async (name: string) => {
      const res = await request.post(`/kubernetes/job/list`, { namespace: name })
      return res.data || res
    }

    export const listCronJob = async (name: string) => {
      const res = await request.post(`/kubernetes/cronJob/list`, { namespace: name })
      return res.data || res
    }
  }

  export namespace EventService {
    /**
     * 获取event列表
     * @param name
     */
    export const listEvent = async (name: string) => {
      const res = await request.post(`/kubernetes/event/list`, { namespace: name })
      return res.data || res
    }
  }

  export namespace ConfigService {
    export const listConfig = async (name: string) => {
      const res = await request.post(`/kubernetes/configMap/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listSecret = async (name: string) => {
      const res = await request.post(`/kubernetes/secret/list`, { namespace: name })
      return res.data || res
    }

    export const listResourceQuota = async (name: string) => {
      const res = await request.post(`/kubernetes/resourceQuota/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listLimitRange = async (name: string) => {
      const res = await request.post(`/kubernetes/limitRange/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listHorizontalPodAutoscaler = async (name: string) => {
      const res = await request.post(`/kubernetes/horizontalPodAutoscaler/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listHorizontalPodAutoscalerV2 = async (name: string) => {
      const res = await request.post(`/kubernetes/horizontalPodAutoscaler/list/v2`, {
        namespace: name
      })
      return res.data || res
    }

    export const listPodDisruptionBudget = async (name: string) => {
      const res = await request.post(`/kubernetes/podDisruptionBudget/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listPriorityClass = async () => {
      const res = await request.get(`/kubernetes/priorityClasses/list`)
      return res.data || res
    }

    export const listRuntimeClass = async () => {
      const res = await request.get(`/kubernetes/runtimeClasses/list`)
      return res.data || res
    }

    export const listLeases = async (name: string) => {
      const res = await request.post(`/kubernetes/leases/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listMutatingWebhookConfiguration = async () => {
      const res = await request.get(`/kubernetes/mutatingWebhookConfiguration/list`)
      return res.data || res
    }

    export const listValidatingWebhookConfiguration = async () => {
      const res = await request.get(`/kubernetes/validatingWebhookConfiguration/list`)
      return res.data || res
    }
  }

  export namespace NetworkService {
    export const listService = async (name: string) => {
      const res = await request.post(`/kubernetes/service/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listEndpoint = async (name: string) => {
      const res = await request.post(`/kubernetes/endpoint/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listPolicy = async (name: string) => {
      const res = await request.post(`/kubernetes/networkPolicy/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listIngress = async (name: string) => {
      const res = await request.post(`/kubernetes/ingress/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const listIngressClass = async () => {
      const res = await request.get(`/kubernetes/ingressClass/list`)
      return res.data || res
    }
  }

  export namespace StorageService {
    export const listPersistentVolumeClaim = async (name: string) => {
      const res = await request.post(`/kubernetes/persistentVolumeClaim/list`, {
        namespace: name
      })
      return res.data || res
    }

    export const getPvcUsdPods = async (name: string) => {
      const res = await request.post(`/kubernetes/persistentVolumeClaim/pods`, {
        namespace: name
      })
      return res.data || res
    }

    export const listPersistentVolume = async () => {
      const res = await request.get(`/kubernetes/persistentVolume/list`)
      return res.data || res
    }

    export const listStorageClass = async () => {
      const res = await request.get(`/kubernetes/storageClass/list`)
      return res.data || res
    }
  }

  export namespace OverviewService {
    export const getWorkloadOverview = async () => {
      const res = await request.get(`/kubernetes/workload/overview`)
      return res.data || res
    }
  }

  export namespace ResourceService {
    export const resourcePatchUpdate = async (param: {}) => {
      const res = await request.post(`/kubernetes/resource/patch/update`, param)
      return res.data || res
    }

    export const resourceCreate = async (param: {}) => {
      const res = await request.post(`/kubernetes/resource/create`, param)
      return res.data || res
    }

    export const resourceDelete = async (param: {}) => {
      const res = await request.post(`/kubernetes/resource/delete`, param)
      return res.data || res
    }

    export const getResourceLogs = async (param: {}) => {
      const res = await request.post(`/kubernetes/resource/logs`, param)
      return res.data || res
    }

    export const resourceUpdate = async (param: {}) => {
      const res = await request.post(`/kubernetes/resource/update`, param)
      return res.data || res
    }
  }
}
