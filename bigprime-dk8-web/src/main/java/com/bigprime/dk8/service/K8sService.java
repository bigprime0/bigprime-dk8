package com.bigprime.dk8.service;

import cn.hutool.json.JSONUtil;
import com.bigprime.dk8.common.config.kubernetes.K8sClientProvider;
import com.bigprime.dk8.common.config.kubernetes.K8sContextHolder;
import com.bigprime.dk8.handler.ClusterConfigHandler;
import com.bigprime.dk8.handler.model.ClusterConfigModel;
import com.bigprime.dk8.vo.*;
import io.kubernetes.client.custom.*;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@RequiredArgsConstructor
public class K8sService {

    private final ClusterConfigHandler clusterHandler;
    private final K8sClientProvider clientProvider;

    //================================================Cluster===========================================================

    /**
     * 集群列表
     *
     * @return
     */
    public List<ClusterConfigModel> listClusters() {
        return clusterHandler.list();
    }

    /**
     * 注册集群
     *
     * @param clusterConfigModel 集群模型
     * @return boolean
     */
    public boolean registerCluster(ClusterConfigModel clusterConfigModel) {
        return clusterHandler.add(clusterConfigModel);
    }

    /**
     * 删除集群
     *
     * @param clusterId 集群标识
     * @return boolean
     */
    public boolean removeCluster(String clusterId) {
        return clusterHandler.remove(clusterId);
    }

    /**
     * 切换集群
     *
     * @param clusterId
     */
    public boolean switchCluster(String clusterId) {
        String currentClusterId = K8sContextHolder.getCurrentClusterId();
        if (currentClusterId == null || currentClusterId.isEmpty() || !currentClusterId.equals(clusterId)) {
            K8sContextHolder.setCurrentClusterId(clusterId);
        }
        return true;
    }

    //================================================Node==============================================================

    public V1NodeList listNodes() throws Exception {
        return clientProvider.coreV1Api().listNode().execute();
    }

    /**
     * 获取节点使用指标
     * Metrics需要依赖k8s的metrics服务，确保k8s集群中了布署了metrics服务</br>
     * <div>确保安装服务：kubectl get deployment metrics-server -n kube-system</div>
     * <div>布署：kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml</div>
     * 如果因为网络问题，可以下载yaml资源文件，修改images节点为镜像<br/>
     * 或是先从国内镜像拉取，再打Tag:
     * <p>docker:</p>
     * docker pull swr.cn-north-4.myhuaweicloud.com/ddn-k8s/registry.k8s.io/metrics-server/metrics-server:v0.7.2 </br>
     * docker tag  swr.cn-north-4.myhuaweicloud.com/ddn-k8s/registry.k8s.io/metrics-server/metrics-server:v0.7.2  registry.k8s.io/metrics-server/metrics-server:v0.7.2
     * <p>containerd</p>
     * ctr images pull swr.cn-north-4.myhuaweicloud.com/ddn-k8s/registry.k8s.io/metrics-server/metrics-server:v0.7.2<br/>
     * ctr images tag  swr.cn-north-4.myhuaweicloud.com/ddn-k8s/registry.k8s.io/metrics-server/metrics-server:v0.7.2  registry.k8s.io/metrics-server/metrics-server:v0.7.2
     */
    public List<NodeMetricsVo> getNodeMetrics() {
        List<NodeMetricsVo> nodeMetricsVos = new ArrayList<>();
        try {
            NodeMetricsList nodeMetricsList = clientProvider.metricsApi().getNodeMetrics();
            for (NodeMetrics nodeMetrics : nodeMetricsList.getItems()) {
                NodeMetricsVo nodeMetricsVo = NodeMetricsVo.builder()
                        .nodeName(nodeMetrics.getMetadata().getName())
                        .cpu(nodeMetrics.getUsage().get("cpu").getNumber())
                        .memory(nodeMetrics.getUsage().get("memory").getNumber())
                        .build();
                nodeMetricsVos.add(nodeMetricsVo);
            }
        } catch (Exception e) {

        }
        return nodeMetricsVos;
    }

    public String nodeCordon(String nodeName) {
        try {
            V1Node node = clientProvider.coreV1Api().readNode(nodeName).execute();
            V1NodeSpec spec = node.getSpec();
            //检查节点是否允许被调度
            if (spec.getUnschedulable() != null && spec.getUnschedulable()) {
                //已经是不可调度状态
            } else {
                //设置节点为不可调度
                spec.setUnschedulable(true);
                //更新节点
                clientProvider.coreV1Api().replaceNode(nodeName, node).execute();
            }
            return "success";
        } catch (Exception ex) {
            return "failure:" + ex.getMessage();
        }
    }

    public String nodeUnCordon(String nodeName) {
        try {
            V1Node node = clientProvider.coreV1Api().readNode(nodeName).execute();
            V1NodeSpec spec = node.getSpec();
            //检查节点是否允许被调度
            if (spec != null && spec.getUnschedulable() != null && spec.getUnschedulable()) {
                //已经是不可调度状态
                spec.setUnschedulable(false);
                clientProvider.coreV1Api().replaceNode(nodeName, node).execute();
            }
            return "success";
        } catch (Exception ex) {
            return "failure:" + ex.getMessage();
        }
    }

    public String nodeDrain(String nodeName) {
        try {
            nodeCordon(nodeName);
            evictPodsOnNode(nodeName);
            return "success";
        } catch (Exception e) {
            return "failure:" + e.getMessage();
        }
    }

    public void evictPodsOnNode(String nodeName) throws Exception {
        // 获取节点上的所有 Pod
        V1PodList podList = clientProvider.coreV1Api().listPodForAllNamespaces().execute();
        // 驱逐每个 Pod
        for (V1Pod pod : podList.getItems()) {
            String podName = pod.getMetadata().getName();
            String namespace = pod.getMetadata().getNamespace();
            // 驱逐 Pod
            clientProvider.coreV1Api().deleteNamespacedPod(podName, namespace).execute();
        }
    }

    //================================================Workload==========================================================

    /**
     * <p>获取Namespace列表</p>
     *
     * @return
     * @throws Exception
     */
    public V1NamespaceList getNamespaces() throws Exception {
        return clientProvider.coreV1Api().listNamespace().execute();
    }

    /**
     * <p>创建命名空间</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1Namespace createNamespace(V1Namespace namespace) throws Exception {
        return clientProvider.coreV1Api().createNamespace(namespace).execute();
    }

    /**
     * <p>删除命名空间</p>
     *
     * @param name
     * @throws ApiException
     */
    public void deleteNamespace(String name) throws Exception {
        clientProvider.coreV1Api().deleteNamespace(name).execute();
    }


    /**
     * <p>获取Pods列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1PodList listPods(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listPodForAllNamespaces().execute();
        } else {
            return clientProvider.coreV1Api().listNamespacedPod(namespace).execute();
        }
    }

    /**
     * <p>获取pod中容器的指标</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public List<PodMetricsVo> getPodMetrics(String namespace) {
        List<PodMetricsVo> metricsVos = new ArrayList<>();
        try {
            PodMetricsList podMetricsList = clientProvider.metricsApi().getPodMetrics(namespace);
            for (PodMetrics podMetrics : podMetricsList.getItems()) {
                PodMetricsVo podMetricsVo = new PodMetricsVo();
                podMetricsVo.setNamespace(namespace);
                podMetricsVo.setPodName(podMetrics.getMetadata().getName());
                List<ContainerMetricsVo> containerMetricsVos = new ArrayList<>();
                podMetrics.getContainers().forEach(container -> {
                    ContainerMetricsVo containerMetricsVo = ContainerMetricsVo.builder()
                            .name(container.getName())
                            .cpu(container.getUsage().get("cpu").getNumber())
                            .memory(container.getUsage().get("memory").getNumber())
                            .build();
                    containerMetricsVos.add(containerMetricsVo);
                });
                podMetricsVo.setContainerMetrics(containerMetricsVos);
                metricsVos.add(podMetricsVo);
            }
        } catch (Exception e) {
        }
        return metricsVos;
    }

    public List<PodMetricsVo> getPodMetrics() throws Exception {
        V1NamespaceList v1NamespaceList = clientProvider.coreV1Api().listNamespace().execute();
        List<PodMetricsVo> results = new ArrayList<>();
        v1NamespaceList.getItems().forEach(item -> {
            try {
                List<PodMetricsVo> podMetrics = getPodMetrics(item.getMetadata().getName());
                results.addAll(podMetrics);
            } catch (Exception e) {

            }
        });
        return results;
    }

    /**
     * <p>获取pod的日志信息</p>
     *
     * @param namespace
     * @param podName
     * @return
     * @throws Exception
     */
    public String getPodLogs(String namespace, String podName) throws Exception {
        return clientProvider.coreV1Api().readNamespacedPodLog(podName, namespace).execute();
    }

    /**
     * <p>获取Deployments列表</p>
     *
     * @param namespace
     * @return
     * @throws ApiException
     */
    public V1DeploymentList listDeployments(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.appsV1Api().listDeploymentForAllNamespaces().execute();
        } else {
            return clientProvider.appsV1Api().listNamespacedDeployment(namespace).execute();
        }
    }

    /**
     * <p>获取DaemonSets列表</p>
     *
     * @param namespace
     * @return
     * @throws ApiException
     */
    public V1DaemonSetList listDaemonSets(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.appsV1Api().listDaemonSetForAllNamespaces().execute();
        } else {
            return clientProvider.appsV1Api().listNamespacedDaemonSet(namespace).execute();
        }
    }

    /**
     * <p>获取StatefulSets列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1StatefulSetList listStatefulSets(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.appsV1Api().listStatefulSetForAllNamespaces().execute();
        } else {
            return clientProvider.appsV1Api().listNamespacedStatefulSet(namespace).execute();
        }
    }

    /**
     * <p>获取ReplicaSets列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1ReplicaSetList listReplicaSets(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.appsV1Api().listReplicaSetForAllNamespaces().execute();
        } else {
            return clientProvider.appsV1Api().listNamespacedReplicaSet(namespace).execute();
        }
    }

    /**
     * <p>获取某个命名空间下的ReplicaSet的状态数据</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public List<ReplicaSetStatusVo> getReplicaSetStatus(String namespace) throws Exception {
        List<ReplicaSetStatusVo> statusList = new ArrayList<>();
        V1ReplicaSetList v1ReplicaSetList = listReplicaSets(namespace);
        v1ReplicaSetList.getItems().forEach(item -> {
            V1ReplicaSet v1ReplicaSet = null;
            try {
                v1ReplicaSet = clientProvider.appsV1Api().readNamespacedReplicaSet(item.getMetadata().getName(), namespace).execute();
                V1ReplicaSetStatus status = v1ReplicaSet.getStatus();
                ReplicaSetStatusVo vo = ReplicaSetStatusVo.builder()
                        .name(item.getMetadata().getName())
                        .desired(status.getReplicas() != null ? status.getReplicas() : 0)
                        .current(status.getAvailableReplicas() != null ? status.getAvailableReplicas() : 0)
                        .ready(status.getReadyReplicas() != null ? status.getReadyReplicas() : 0)
                        .build();
                statusList.add(vo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return statusList;
    }

    /**
     * <p>获取所有ReplicaSet状态数据</p>
     *
     * @return
     * @throws Exception
     */
    public List<ReplicaSetStatusVo> getReplicaSetStatus() throws Exception {
        V1NamespaceList v1NamespaceList = clientProvider.coreV1Api().listNamespace().execute();
        List<ReplicaSetStatusVo> results = new ArrayList<>();
        v1NamespaceList.getItems().forEach(item -> {
            try {
                List<ReplicaSetStatusVo> statusVo = getReplicaSetStatus(item.getMetadata().getName());
                if (statusVo.size() > 0) {
                    results.addAll(statusVo);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return results;
    }

    /**
     * <p>获取Jobs列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1JobList listJobs(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.batchV1Api().listJobForAllNamespaces().execute();
        } else {
            return clientProvider.batchV1Api().listNamespacedJob(namespace).execute();
        }
    }

    /**
     * <p>获取CronJobs列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1CronJobList listCronJobs(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.batchV1Api().listCronJobForAllNamespaces().execute();
        } else {
            return clientProvider.batchV1Api().listNamespacedCronJob(namespace).execute();
        }
    }

    /**
     * <p>获取ReplicationControllers列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1ReplicationControllerList listReplicationControllers(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listReplicationControllerForAllNamespaces().execute();
        } else {
            return clientProvider.coreV1Api().listNamespacedReplicationController(namespace).execute();
        }
    }

    //=================================================Config===========================================================

    /**
     * <p>获取ConfigMap集合</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1ConfigMapList listConfigMaps(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listConfigMapForAllNamespaces().execute();
        } else {
            return clientProvider.coreV1Api().listNamespacedConfigMap(namespace).execute();
        }
    }

    /**
     * <p>获取Secret列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1SecretList listSecrets(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listSecretForAllNamespaces().execute();
        } else {
            return clientProvider.coreV1Api().listNamespacedSecret(namespace).execute();
        }
    }

    /**
     * <p>获取ResourceQuota列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1ResourceQuotaList listResourceQuota(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listResourceQuotaForAllNamespaces().execute();
        }
        return clientProvider.coreV1Api().listNamespacedResourceQuota(namespace).execute();

    }

    /**
     * <p>获取LimitRange列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1LimitRangeList listLimitRanges(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listLimitRangeForAllNamespaces().execute();
        }
        return clientProvider.coreV1Api().listNamespacedLimitRange(namespace).execute();
    }

    /**
     * <p>获取HorizontalPodAutoscaler列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1HorizontalPodAutoscalerList listHorizontalPodAutoscalers(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.autoscalingV1Api().listHorizontalPodAutoscalerForAllNamespaces().execute();
        }
        return clientProvider.autoscalingV1Api().listNamespacedHorizontalPodAutoscaler(namespace).execute();
    }

    public V2HorizontalPodAutoscalerList listV2HorizontalPodAutoscalers(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.autoscalingV2Api().listHorizontalPodAutoscalerForAllNamespaces().execute();
        }
        return clientProvider.autoscalingV2Api().listNamespacedHorizontalPodAutoscaler(namespace).execute();
    }

    /**
     * <p>获取PodDisruptionBudget列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1PodDisruptionBudgetList listPodDisruptionBudgets(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.policyV1Api().listPodDisruptionBudgetForAllNamespaces().execute();
        }
        return clientProvider.policyV1Api().listNamespacedPodDisruptionBudget(namespace).execute();
    }

    /**
     * <p>获取PriorityClass列表</p>
     * <h3>PriorityClass</h3>
     * <p>PriorityClass在Kubernetes中用于定义Pod的调度优先级。它允许集群管理员为不同的Pod分配不同的优先级，确保关键任务在资源紧张时优先调度。PriorityClass通过设置整数值来确定优先级，数值越高，优先级越高。</p>
     * <p>1、创建PriorityClass： 使用YAML文件定义PriorityClass，指定其名称和优先级值，然后应用到集群中。</p>
     * <pre>
     * --priorityClass.yaml
     *   apiVersion: scheduling.k8s.io/v1
     *   kind: PriorityClass
     *   metadata:
     *     name: high-priority
     *   value: 100000
     * </pre>
     * <p>2、在Pod中使用PriorityClass： 在Pod的定义中spec下指定priorityClassName字段</p>
     * <pre>
     *   spec:
     *     priorityClassName: high-priority
     * </pre>
     *
     * @return
     * @throws Exception
     */
    public V1PriorityClassList listPriorityClasses() throws Exception {
        return clientProvider.schedulingV1Api().listPriorityClass().execute();
    }

    /**
     * <p>获取RuntimeClass列表</p>
     * <p>RuntimeClass</p>
     * <p>RuntimeClass在Kubernetes中用于指定Pod使用的容器运行时（如Docker、containerd、CRI-O等）。这允许集群管理员配置不同的运行时环境，供用户在创建Pod时选择，满足特定应用的需求。</p>
     * <p>示例：</p>
     * <p>  1、定义RuntimeClass</p>
     * <pre>
     *     apiVersion: node.k8s.io/v1
     *     kind: RuntimeClass
     *     metadata:
     *       name: my-runtime-class
     *     handler: "containerd"
     * </pre>
     * <p>  2、在Pod中使用RuntimeClass： 用户在Pod的spec中指定runtimeClassName字段，引用已定义的RuntimeClass</p>
     * <pre>
     *     spec:
     *       runtimeClassName: my-runtime-class
     * </pre>
     *
     * @return
     * @throws Exception
     */
    public V1RuntimeClassList listRuntimeClasses() throws Exception {
        return clientProvider.nodeV1Api().listRuntimeClass().execute();
    }

    /**
     * <p>获Leases列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1LeaseList listLeases(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coordinationV1Api().listLeaseForAllNamespaces().execute();
        }
        return clientProvider.coordinationV1Api().listNamespacedLease(namespace).execute();
    }

    /**
     * <p>获取MutatingWebhookConfiguration列表</p>
     *
     * @return
     * @throws Exception
     */
    public V1MutatingWebhookConfigurationList listMutatingWebhookConfigurations() throws Exception {
        return clientProvider.admissionregistrationV1Api().listMutatingWebhookConfiguration().execute();
    }

    /**
     * <p>获取ValidatingWebhookConfiguration列表</p>
     *
     * @return
     * @throws Exception
     */
    public V1ValidatingWebhookConfigurationList listValidatingWebhookConfigurations() throws Exception {
        return clientProvider.admissionregistrationV1Api().listValidatingWebhookConfiguration().execute();
    }

    //= ===================================================NetWork======================================================

    /**
     * <p>获取Service列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1ServiceList listServices(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listServiceForAllNamespaces().execute();
        }
        return clientProvider.coreV1Api().listNamespacedService(namespace).execute();
    }

    /**
     * <p>获取Endpoint列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1EndpointsList listEndpoints(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listEndpointsForAllNamespaces().execute();
        }
        return clientProvider.coreV1Api().listNamespacedEndpoints(namespace).execute();
    }

    /**
     * <p>获取NetworkPolicy列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1NetworkPolicyList listNetworkPolicies(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.networkingV1Api().listNetworkPolicyForAllNamespaces().execute();
        }
        return clientProvider.networkingV1Api().listNamespacedNetworkPolicy(namespace).execute();
    }

    /**
     * <p>获取Ingress列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1IngressList listIngress(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.networkingV1Api().listIngressForAllNamespaces().execute();
        }
        return clientProvider.networkingV1Api().listNamespacedIngress(namespace).execute();
    }

    /**
     * <p>获取IngressClass列表</p>
     *
     * @return
     * @throws Exception
     */
    public V1IngressClassList listIngressClasses() throws Exception {
        return clientProvider.networkingV1Api().listIngressClass().execute();
    }

    //=================================================Storage==========================================================

    /**
     * <p>获取PVC持久卷声明列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1PersistentVolumeClaimList listPersistentVolumeClaims(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listPersistentVolumeClaimForAllNamespaces().execute();
        }
        return clientProvider.coreV1Api().listNamespacedPersistentVolumeClaim(namespace).execute();
    }

    /**
     * <p>获取pod申请的pvc</p>
     *
     * @param namespace
     * @return
     * @throws ApiException
     */
    public Map<String, String> getPVCUsePods(String namespace) throws Exception {
        V1PersistentVolumeClaimList v1PersistentVolumeClaimList = listPersistentVolumeClaims(namespace);
        V1PodList v1PodList = listPods(namespace);
        Map<String, String> pvcPodMap = new HashMap<String, String>();
        v1PersistentVolumeClaimList.getItems().forEach(pvc -> {
            v1PodList.getItems().forEach(pod -> {
                if (pod.getSpec() != null && pod.getSpec().getVolumes() != null) {
                    for (V1Volume volume : pod.getSpec().getVolumes()) {
                        if (volume.getPersistentVolumeClaim() != null && pvc.getMetadata().getName().equals(volume.getPersistentVolumeClaim().getClaimName())) {
                            pvcPodMap.put(pvc.getMetadata().getName(), pod.getMetadata().getName());
                            break;
                        }
                    }
                }

            });
        });
        return pvcPodMap;
    }

    /**
     * <p>获取PV持久卷列表</p>
     *
     * @return
     * @throws Exception
     */
    public V1PersistentVolumeList listPersistentVolumeList() throws Exception {
        return clientProvider.coreV1Api().listPersistentVolume().execute();
    }

    /**
     * <p>获取StorageClass列表</p>
     *
     * @return
     * @throws ApiException
     */
    public V1StorageClassList listStorageClasses() throws Exception {
        return clientProvider.storageV1Api().listStorageClass().execute();
    }

    //====================================================AccessControl=================================================

    /**
     * <p>获取ServiceAccount列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1ServiceAccountList listServiceAccounts(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listServiceAccountForAllNamespaces().execute();
        }
        return clientProvider.coreV1Api().listNamespacedServiceAccount(namespace).execute();
    }

    /**
     * <p>获取ClusterRole列表</p>
     *
     * @return
     * @throws Exception
     */
    public V1ClusterRoleList listClusterRoles() throws Exception {
        return clientProvider.rbacAuthorizationV1Api().listClusterRole().execute();
    }

    /**
     * <p>获取Role列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1RoleList listRoles(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.rbacAuthorizationV1Api().listRoleForAllNamespaces().execute();
        }
        return clientProvider.rbacAuthorizationV1Api().listNamespacedRole(namespace).execute();
    }

    /**
     * <p>获取ClusterRoleBinding列表</p>
     *
     * @return
     * @throws Exception
     */
    public V1ClusterRoleBindingList listClusterRoleBindings() throws Exception {
        return clientProvider.rbacAuthorizationV1Api().listClusterRoleBinding().execute();
    }

    /**
     * <p>获取RoleBinding列表</p>
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public V1RoleBindingList listRoleBindings(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.rbacAuthorizationV1Api().listRoleBindingForAllNamespaces().execute();
        }
        return clientProvider.rbacAuthorizationV1Api().listNamespacedRoleBinding(namespace).execute();
    }

    /**
     * 获取Event列表
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    public CoreV1EventList listEvents(String namespace) throws Exception {
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return clientProvider.coreV1Api().listEventForAllNamespaces().execute();
        }
        return clientProvider.coreV1Api().listNamespacedEvent(namespace).execute();
    }

    //=======================================================overview===================================================
    public List<WorkloadOverviewVo> getWorkloadOverview() throws Exception {
        List<WorkloadOverviewVo> result = new ArrayList<>();
        V1PodList v1PodList = listPods("--all");
        int podRunningCount = (int) v1PodList.getItems().stream().filter(pod -> pod.getStatus().getPhase().equals("Running")).count();
        result.add(WorkloadOverviewVo.builder().workload("Pods").runningCount(podRunningCount).unRunningCount(v1PodList.getItems().size() - podRunningCount).build());
        V1DeploymentList v1DeploymentList = listDeployments("--all");

        AtomicInteger deploymentRunningCount = new AtomicInteger();
        v1DeploymentList.getItems().forEach(d -> {
            if (d.getStatus().getAvailableReplicas() == d.getStatus().getReadyReplicas()) {
                V1DeploymentCondition available = d.getStatus().getConditions().stream().filter(c -> c.getType().equals("Available")).findFirst().get();
                V1DeploymentCondition progressing = d.getStatus().getConditions().stream().filter(c -> c.getType().equals("Progressing")).findFirst().get();
                if (available.getStatus().equals("True") && progressing.getStatus().equals("True")) {
                    deploymentRunningCount.addAndGet(1);
                }
            }
        });
        result.add(WorkloadOverviewVo.builder().workload("Deployments").runningCount(deploymentRunningCount.get()).unRunningCount(v1DeploymentList.getItems().size() - deploymentRunningCount.get()).build());

        V1DaemonSetList v1DaemonSetList = listDaemonSets("--all");
        result.add(WorkloadOverviewVo.builder().workload("DaemonSets").runningCount(v1DaemonSetList.getItems().size()).build());

        V1StatefulSetList v1StatefulSetList = listStatefulSets("--all");
        result.add(WorkloadOverviewVo.builder().workload("StatefulSets").runningCount(v1StatefulSetList.getItems().size()).build());

        V1ReplicaSetList v1ReplicaSetList = listReplicaSets("--all");
        result.add(WorkloadOverviewVo.builder().workload("ReplicaSets").runningCount(v1ReplicaSetList.getItems().size()).build());

        V1JobList v1JobList = listJobs("--all");
        result.add(WorkloadOverviewVo.builder().workload("Jobs").runningCount(v1JobList.getItems().size()).build());

        return result;
    }

    //=======================================================资源局部更新====================================================
    public String patchUpdateResource(String namespace, String resType, String resName, String patchContent) {
        String resp = "";
        try {
            switch (resType) {
                case "Deployment":
                    V1Deployment deployment = clientProvider.appsV1Api().patchNamespacedDeployment(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(deployment);
                    break;
                case "StatefulSet":
                    V1StatefulSet statefulSet = clientProvider.appsV1Api().patchNamespacedStatefulSet(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(statefulSet);
                    break;
                case "ReplicaSet":
                    V1ReplicaSet replicaSet = clientProvider.appsV1Api().patchNamespacedReplicaSet(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(replicaSet);
                    break;
                case "DaemonSet":
                    V1DaemonSet daemonSet = clientProvider.appsV1Api().patchNamespacedDaemonSet(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(daemonSet);
                    break;
                case "Pod":
                    V1Pod pod = clientProvider.coreV1Api().patchNamespacedPod(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(pod);
                    break;
                case "ConfigMap":
                    V1ConfigMap configMap = clientProvider.coreV1Api().patchNamespacedConfigMap(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(configMap);
                    break;
                case "LimitRange":
                    V1LimitRange limitRange = clientProvider.coreV1Api().patchNamespacedLimitRange(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(limitRange);
                    break;
                case "Endpoints":
                    V1Endpoints endpoints = clientProvider.coreV1Api().patchNamespacedEndpoints(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(endpoints);
                    break;
                case "PVC":
                    V1PersistentVolumeClaim pvc = clientProvider.coreV1Api().patchNamespacedPersistentVolumeClaim(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(pvc);
                    break;
                case "ReplicationController":
                    V1ReplicationController replicationController = clientProvider.coreV1Api().patchNamespacedReplicationController(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(replicationController);
                    break;
                case "ResourceQuota":
                    V1ResourceQuota execute = clientProvider.coreV1Api().patchNamespacedResourceQuota(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(execute);
                    break;
                case "Secret":
                    V1Secret secret = clientProvider.coreV1Api().patchNamespacedSecret(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(secret);
                    break;
                case "Service":
                    V1Service service = clientProvider.coreV1Api().patchNamespacedService(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(service);
                    break;
                case "Ingress":
                    V1Ingress ingress = clientProvider.networkingV1Api().patchNamespacedIngress(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(ingress);
                    break;
                case "IngressClass":
                    V1IngressClass ingressClass = clientProvider.networkingV1Api().patchIngressClass(resName, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(ingressClass);
                    break;
                case "NetworkPolicy":
                    V1NetworkPolicy networkPolicy = clientProvider.networkingV1Api().patchNamespacedNetworkPolicy(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(networkPolicy);
                    break;
                case "CronJob":
                    V1CronJob cronJob = clientProvider.batchV1Api().patchNamespacedCronJob(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(cronJob);
                    break;
                case "Job":
                    V1Job job = clientProvider.batchV1Api().patchNamespacedJob(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(job);
                    break;
                case "MWC":
                    V1MutatingWebhookConfiguration mwc = clientProvider.admissionregistrationV1Api().patchMutatingWebhookConfiguration(resName, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(mwc);
                    break;
                case "VWC":
                    V1ValidatingWebhookConfiguration vwc = clientProvider.admissionregistrationV1Api().patchValidatingWebhookConfiguration(resName, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(vwc);
                    break;
                case "StorageClass":
                    V1StorageClass storageClass = clientProvider.storageV1Api().patchStorageClass(resName, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(storageClass);
                    break;
                case "HPA":
                    V1HorizontalPodAutoscaler hpa = clientProvider.autoscalingV1Api().patchNamespacedHorizontalPodAutoscaler(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(hpa);
                    break;
                case "Lease":
                    V1Lease lease = clientProvider.coordinationV1Api().patchNamespacedLease(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(lease);
                    break;
                case "Namespace":
                    V1Namespace ns = clientProvider.coreV1Api().patchNamespace(resName, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(ns);
                    break;
                case "Budget":
                    V1PodDisruptionBudget budget = clientProvider.policyV1Api().patchNamespacedPodDisruptionBudget(resName, namespace, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(budget);
                    break;
                case "Role":
                    V1ClusterRole role = clientProvider.rbacAuthorizationV1Api().patchClusterRole(resName, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(role);
                    break;
                case "RoleBinding":
                    V1ClusterRoleBinding roleBinding = clientProvider.rbacAuthorizationV1Api().patchClusterRoleBinding(resName, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(roleBinding);
                    break;
                case "Priority":
                    V1PriorityClass priority = clientProvider.schedulingV1Api().patchPriorityClass(resName, new V1Patch(patchContent)).execute();
                    resp = JSONUtil.toJsonStr(priority);
                    break;
            }
        } catch (Exception e) {
            resp = "Failure:" + e.getMessage();
        }
        return resp;
    }

    public String createResource(String resType, String yamlStr) {
        String resp = "";
        try {
            if (resType.equalsIgnoreCase("Pod")) {
                Constructor constructor = new Constructor(V1Pod.class);
                Yaml yaml = new Yaml(constructor);
                V1Pod pod = yaml.loadAs(yamlStr, V1Pod.class);
                String namespace = Objects.requireNonNull(pod.getMetadata()).getNamespace();
                if (namespace == null || namespace.isEmpty()) {
                    namespace = "default";
                }
                if (pod.getSpec() != null) {
                    pod.getSpec().setOverhead(null); //不设为null则yaml中必须指定与OverHead匹配的RuntimeClass,否则报403错误
                }
                V1Pod created = clientProvider.coreV1Api().createNamespacedPod(namespace, pod).execute();
                resp = "Success：" + created.getMetadata().getName();
            } else if (resType.equalsIgnoreCase("Namespace")) {
                Constructor constructor = new Constructor(V1Namespace.class);
                Yaml yaml = new Yaml(constructor);
                V1Namespace ns = yaml.loadAs(yamlStr, V1Namespace.class);
                V1Namespace created = clientProvider.coreV1Api().createNamespace(ns).execute();
                resp = "Success：" + created.getMetadata().getName();
            } else if (resType.equalsIgnoreCase("Deployment")) {
                Constructor constructor = new Constructor(V1Deployment.class);
                Yaml yaml = new Yaml(constructor);
                V1Deployment body = yaml.loadAs(yamlStr, V1Deployment.class);
                String namespace = Objects.requireNonNull(body.getMetadata()).getNamespace();
                V1Deployment created = clientProvider.appsV1Api().createNamespacedDeployment(namespace, body).execute();
                resp = "Success：" + created.getMetadata().getName();
            } else {
                resp = "Unknown: Unrecognized yaml";
            }
        } catch (Exception e) {
            resp = "Failure:" + e.getMessage();
        }
        return resp;
    }

    public String deleteResource(String resType, String resName, String namespace) {
        String resp = "";
        try {
            if (resType.equalsIgnoreCase("Namespace")) {
                clientProvider.coreV1Api().deleteNamespace(resName).execute();
            }
            if (resType.equalsIgnoreCase("Pod")) {
                clientProvider.coreV1Api().deleteNamespacedPod(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("Deployment")) {
                clientProvider.appsV1Api().deleteNamespacedDeployment(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("DaemonSet")) {
                clientProvider.appsV1Api().deleteNamespacedDaemonSet(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("StatefulSet")) {
                clientProvider.appsV1Api().deleteNamespacedStatefulSet(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("CronJob")) {
                clientProvider.batchV1Api().deleteNamespacedCronJob(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("Job")) {
                clientProvider.batchV1Api().deleteNamespacedJob(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("ReplicaSet")) {
                clientProvider.appsV1Api().deleteNamespacedReplicaSet(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("ConfigMap")) {
                clientProvider.coreV1Api().deleteNamespacedConfigMap(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("HPA")) {
                clientProvider.autoscalingV2Api().deleteNamespacedHorizontalPodAutoscaler(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("Secret")) {
                clientProvider.coreV1Api().deleteNamespacedSecret(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("Ingress")) {
                clientProvider.networkingV1Api().deleteNamespacedIngress(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("Service")) {
                clientProvider.coreV1Api().deleteNamespacedService(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("PV")) {
                clientProvider.coreV1Api().deletePersistentVolume(resName).execute();
            }
            if (resType.equalsIgnoreCase("PVC")) {
                clientProvider.coreV1Api().deleteNamespacedPersistentVolumeClaim(resName, namespace).execute();
            }
            if (resType.equalsIgnoreCase("StorageClass")) {
                clientProvider.storageV1Api().deleteStorageClass(resName).execute();
            }
            resp = "Success";
        } catch (Exception e) {
            resp = "Failure:" + e.getMessage();
        }
        return resp;
    }

    public String getResourceLogs(String resType, String resName, String namespace) {
        StringBuilder resp = new StringBuilder();
        try {
            if (resType.equalsIgnoreCase("Pod")) {
                resp = new StringBuilder(clientProvider.coreV1Api().readNamespacedPodLog(resName, namespace).execute());
            }
        } catch (Exception e) {
            resp = new StringBuilder("Failure:" + e.getMessage());
        }
        return resp.toString();
    }

    public String updateResource(String resType, String resName, String namespace, String content) {
        StringBuilder resp = new StringBuilder();
        try {
            if (resType.equalsIgnoreCase("Pod")) {
                V1Pod source = clientProvider.coreV1Api().readNamespacedPod(resName, namespace).execute();
                V1Pod update = JSONUtil.toBean(content, V1Pod.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.coreV1Api().replaceNamespacedPod(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("Deployment")) {
                V1Deployment source = clientProvider.appsV1Api().readNamespacedDeployment(resName, namespace).execute();
                V1Deployment update = JSONUtil.toBean(content, V1Deployment.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.appsV1Api().replaceNamespacedDeployment(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("DaemonSet")) {
                V1DaemonSet source = clientProvider.appsV1Api().readNamespacedDaemonSet(resName, namespace).execute();
                V1DaemonSet update = JSONUtil.toBean(content, V1DaemonSet.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.appsV1Api().replaceNamespacedDaemonSet(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("StatefulSet")) {
                V1StatefulSet source = clientProvider.appsV1Api().readNamespacedStatefulSet(resName, namespace).execute();
                V1StatefulSet update = JSONUtil.toBean(content, V1StatefulSet.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.appsV1Api().replaceNamespacedStatefulSet(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("CronJob")) {
                V1CronJob source = clientProvider.batchV1Api().readNamespacedCronJob(resName, namespace).execute();
                V1CronJob update = JSONUtil.toBean(content, V1CronJob.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.batchV1Api().replaceNamespacedCronJob(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("Job")) {
                V1Job source = clientProvider.batchV1Api().readNamespacedJob(resName, namespace).execute();
                V1Job update = JSONUtil.toBean(content, V1Job.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.batchV1Api().replaceNamespacedJob(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("ReplicaSet")) {
                V1ReplicaSet source = clientProvider.appsV1Api().readNamespacedReplicaSet(resName, namespace).execute();
                V1ReplicaSet update = JSONUtil.toBean(content, V1ReplicaSet.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.appsV1Api().replaceNamespacedReplicaSet(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("ConfigMap")) {
                V1ConfigMap source = clientProvider.coreV1Api().readNamespacedConfigMap(resName, namespace).execute();
                V1ConfigMap update = JSONUtil.toBean(content, V1ConfigMap.class);
                source.setMetadata(update.getMetadata());
                source.setData(update.getData());
                clientProvider.coreV1Api().replaceNamespacedConfigMap(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("HPA")) {
                V1HorizontalPodAutoscaler source = clientProvider.autoscalingV1Api().readNamespacedHorizontalPodAutoscaler(resName, namespace).execute();
                V1HorizontalPodAutoscaler update = JSONUtil.toBean(content, V1HorizontalPodAutoscaler.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.autoscalingV1Api().replaceNamespacedHorizontalPodAutoscaler(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("Secret")) {
                V1Secret source = clientProvider.coreV1Api().readNamespacedSecret(resName, namespace).execute();
                V1Secret update = JSONUtil.toBean(content, V1Secret.class);
                source.setMetadata(update.getMetadata());
                source.setData(update.getData());
                clientProvider.coreV1Api().replaceNamespacedSecret(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("Ingress")) {
                V1Ingress source = clientProvider.networkingV1Api().readNamespacedIngress(resName, namespace).execute();
                V1Ingress update = JSONUtil.toBean(content, V1Ingress.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.networkingV1Api().replaceNamespacedIngress(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("Service")) {
                V1Service source = clientProvider.coreV1Api().readNamespacedService(resName, namespace).execute();
                V1Service update = JSONUtil.toBean(content, V1Service.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.coreV1Api().replaceNamespacedService(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("PV")) {
                V1PersistentVolume source = clientProvider.coreV1Api().readPersistentVolume(resName).execute();
                V1PersistentVolume update = JSONUtil.toBean(content, V1PersistentVolume.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.coreV1Api().replacePersistentVolume(resName, source).execute();
            }
            if (resType.equalsIgnoreCase("PVC")) {
                V1PersistentVolumeClaim source = clientProvider.coreV1Api().readNamespacedPersistentVolumeClaim(resName, namespace).execute();
                V1PersistentVolumeClaim update = JSONUtil.toBean(content, V1PersistentVolumeClaim.class);
                source.setMetadata(update.getMetadata());
                source.setSpec(update.getSpec());
                clientProvider.coreV1Api().replaceNamespacedPersistentVolumeClaim(resName, namespace, source).execute();
            }
            if (resType.equalsIgnoreCase("StorageClass")) {
                V1StorageClass source = clientProvider.storageV1Api().readStorageClass(resName).execute();
                V1StorageClass update = JSONUtil.toBean(content, V1StorageClass.class);
                source.setMetadata(update.getMetadata());
                source.setVolumeBindingMode(update.getVolumeBindingMode());
                source.setMountOptions(update.getMountOptions());
                source.setParameters(update.getParameters());
                source.setReclaimPolicy(update.getReclaimPolicy());
                clientProvider.storageV1Api().replaceStorageClass(resName, source).execute();
            }
            resp.append("Success");
        } catch (Exception e) {
            resp = new StringBuilder("Failure:" + e.getMessage());
        }
        return resp.toString();
    }
}
