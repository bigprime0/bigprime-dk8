import NProgress from 'nprogress'
import { createRouter, createWebHashHistory } from 'vue-router'
import { REDIRECT_PATH } from '@/config/setting'
import DefaultLayout from '@/layout/default-layout.vue'
import IframeLayout from '@/layout/iframe-layout.vue'
import LoginView from '@/views/login/login.vue'
import { getToken } from '@/utils/token'
import { isEmpty } from 'lodash-es'
import IframeChild from '@/views/components/iframe-child.vue'
import Cluster from '@/views/kubernetes/cluster/cluster.vue'
import Node from '@/views/kubernetes/node/index.vue'
import Namespace from '@/views/kubernetes/namespace/index.vue'
import Overview from '@/views/kubernetes/overview.vue'
//workloads
import Pod from '@/views/kubernetes/workload/pods.vue'
import Deployment from '@/views/kubernetes/workload/deployments.vue'
import DaemonSet from '@/views/kubernetes/workload/daemonSets.vue'
import StatefulSet from '@/views/kubernetes/workload/statefulSets.vue'
import ReplicaSet from '@/views/kubernetes/workload/replicaSets.vue'
import ReplicationController from '@/views/kubernetes/workload/replicationControllers.vue'
import Job from '@/views/kubernetes/workload/jobs.vue'
import CronJob from '@/views/kubernetes/workload/cronJobs.vue'
//Config
import ConfigMap from '@/views/kubernetes/config/configMaps.vue'
import Secret from '@/views/kubernetes/config/secret.vue'
import Resourcequota from '@/views/kubernetes/config/resourceQuotas.vue'
import LimitRange from '@/views/kubernetes/config/limitRanges.vue'
import Horizontalpodautoscaler from '@/views/kubernetes/config/horizontalPodAutoscaler.vue'
import PodDisruptionBudget from '@/views/kubernetes/config/podDisruptionBudget.vue'
import Priorityclass from '@/views/kubernetes/config/priorityClass.vue'
import Lease from '@/views/kubernetes/config/leases.vue'
import RuntimeClass from '@/views/kubernetes/config/runtimeClass.vue'
import ValidatingWebhookConfiguration from '@/views/kubernetes/config/validatingWebhookConfiguration.vue'
import MutatingWebhookConfiguration from '@/views/kubernetes/config/mutatingWebhookConfiguration.vue'
//Network
import Service from '@/views/kubernetes/network/service.vue'
import Endpoint from '@/views/kubernetes/network/endpoint.vue'
import Ingress from '@/views/kubernetes/network/ingress.vue'
import IngressClass from '@/views/kubernetes/network/ingressClass.vue'
import Policy from '@/views/kubernetes/network/policy.vue'
//Storage
import PVC from '@/views/kubernetes/storage/pvc.vue'
import PV from '@/views/kubernetes/storage/pv.vue'
import StorageClass from '@/views/kubernetes/storage/storageClass.vue'
//Event
import Events from '@/views/kubernetes/event/index.vue'
//Terminal
import Terminal from '@/views/kubernetes/shell/TerminalPage.vue'
//KWS
import KWS from '@/views/kubernetes/kws/index.vue'

NProgress.configure({
  speed: 200,
  minimum: 0.02,
  trickleSpeed: 200,
  showSpinner: false
})

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      requiresAuth: false
    }
  },
  {
    name: 'home',
    path: import.meta.env.VITE_CONTEXT || '/home',
    component: DefaultLayout,
    children: [
      {
        path: '/cluster',
        name: 'cluster',
        label: 'menu.kubernetes.cluster',
        component: Cluster,
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/overview',
        name: 'overview',
        label: 'menu.kubernetes.overview',
        component: Overview,
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/node',
        name: 'node',
        label: 'menu.kubernetes.node',
        component: Node,
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/namespace',
        name: 'namespace',
        label: 'menu.kubernetes.namespace',
        component: Namespace,
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/workloads',
        name: 'workloads',
        label: 'menu.kubernetes.workloads',
        children: [
          // {
          //   path: '/overview',
          //   name: 'overview',
          //   label: 'menu.kubernetes.overview',
          //   component: Overview,
          //   meta: {
          //     requiresAuth: true
          //   }
          // },
          {
            path: '/pod',
            name: 'pod',
            label: 'menu.kubernetes.workloads.pods',
            component: Pod,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/deployment',
            name: 'deployment',
            label: 'menu.kubernetes.workloads.deployments',
            component: Deployment,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/daemonSet',
            name: 'daemonSet',
            label: 'menu.kubernetes.workloads.daemonSets',
            component: DaemonSet,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/statefulSet',
            name: 'statefulSet',
            label: 'menu.kubernetes.workloads.statefulsets',
            component: StatefulSet,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/replicaSet',
            name: 'replicaSet',
            label: 'menu.kubernetes.workloads.replicasets',
            component: ReplicaSet,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/replicationController',
            name: 'replicationController',
            label: 'menu.kubernetes.workloads.replicationsontrollers',
            component: ReplicationController,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/job',
            name: 'job',
            label: 'menu.kubernetes.workloads.jobs',
            component: Job,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/cronJob',
            name: 'cronJob',
            label: 'menu.kubernetes.workloads.cronjobs',
            component: CronJob,
            meta: {
              requiresAuth: true
            }
          }
        ],
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/config',
        name: 'config',
        label: 'menu.kubernetes.config',
        children: [
          {
            path: '/configMap',
            name: 'configMap',
            label: 'menu.kubernetes.config.configmaps',
            component: ConfigMap,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/secret',
            name: 'secret',
            label: 'menu.kubernetes.config.secret',
            component: Secret,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/resourcequota',
            name: 'resourcequota',
            label: 'menu.kubernetes.config.resourcequota',
            component: Resourcequota,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/limitRanges',
            name: 'limitRanges',
            label: 'menu.kubernetes.config.limitrange',
            component: LimitRange,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/horizontalPodAutoscaler',
            name: 'horizontalPodAutoscaler',
            label: 'menu.kubernetes.config.horizontalpodautoscaler',
            component: Horizontalpodautoscaler,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/podDisruptionBudget',
            name: 'podDisruptionBudget',
            label: 'menu.kubernetes.config.poddisruptionbudget',
            component: PodDisruptionBudget,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/priorityclass',
            name: 'priorityclass',
            label: 'menu.kubernetes.config.priorityclass',
            component: Priorityclass,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/runtimeClass',
            name: 'runtimeClass',
            label: 'menu.kubernetes.config.runtimeclasses',
            component: RuntimeClass,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/lease',
            name: 'lease',
            label: 'menu.kubernetes.config.leases',
            component: Lease,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/validatingWebhookConfiguration',
            name: 'validatingWebhookConfiguration',
            label: 'menu.kubernetes.config.validatingwebhookconfiguration',
            component: ValidatingWebhookConfiguration,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/mutatingWebhookConfiguration',
            name: 'mutatingWebhookConfiguration',
            label: 'menu.kubernetes.config.mutatingwebhookconfiguration',
            component: MutatingWebhookConfiguration,
            meta: {
              requiresAuth: true
            }
          }
        ],
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/network',
        name: 'network',
        label: 'menu.kubernetes.network',
        children: [
          {
            path: '/service',
            name: 'service',
            label: 'menu.kubernetes.network.service',
            component: Service,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/endpoint',
            name: 'endpoint',
            label: 'menu.kubernetes.network.endpoint',
            component: Endpoint,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/ingress',
            name: 'ingress',
            label: 'menu.kubernetes.network.ingress',
            component: Ingress,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/ingressClass',
            name: 'ingressClass',
            label: 'menu.kubernetes.network.ingressclass',
            component: IngressClass,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/policy',
            name: 'policy',
            label: 'menu.kubernetes.network.policy',
            component: Policy,
            meta: {
              requiresAuth: true
            }
          }
        ],
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/storage',
        name: 'storage',
        label: 'menu.kubernetes.storage',
        children: [
          {
            path: '/pvc',
            name: 'pvc',
            label: 'menu.kubernetes.storage.pvc',
            component: PVC,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/pv',
            name: 'pv',
            label: 'menu.kubernetes.storage.pv',
            component: PV,
            meta: {
              requiresAuth: true
            }
          },
          {
            path: '/storageClass',
            name: 'storageClass',
            label: 'menu.kubernetes.storage.storageclass',
            component: StorageClass,
            meta: {
              requiresAuth: true
            }
          }
        ],
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/event',
        name: 'event',
        label: 'menu.kubernetes.events',
        component: Events,
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/terminal',
        name: 'terminal',
        label: 'menu.kubernetes.shell',
        component: Terminal,
        meta: {
          requiresAuth: true
        }
      },
      {
        path: '/kws',
        name: 'kws',
        label: 'menu.kubernetes.kws',
        component: KWS,
        meta: {
          requiresAuth: true
        }
      }
    ],
    meta: {
      requiresAuth: true
    }
  },
  {
    name: 'iframe',
    path: '/iframe',
    component: IframeLayout,
    children: [
      {
        path: '/iframe/page',
        name: 'iframe-page',
        component: IframeChild,
        meta: {
          requiresAuth: false
        }
      }
    ],
    meta: {
      requiresAuth: false
    }
  }
]

//创建路由映射表、路由对象
const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

//路由前置守卫,验证用户身份，权限等
//to: 即将进入目标路由对象，包含路径、参数、查询参数等
//from:当前导航正要离开的路由对象
router.beforeEach(async (to, from) => {
  const token = getToken()
  if (isEmpty(token) && to.meta.requiresAuth) {
    return { name: 'login' }
  }
})

router.afterEach(async (to) => {
  if (!to.path.includes(REDIRECT_PATH) && NProgress.isStarted()) {
    setTimeout(() => {
      NProgress.done(true)
    }, 200)
  }
})

export default router
