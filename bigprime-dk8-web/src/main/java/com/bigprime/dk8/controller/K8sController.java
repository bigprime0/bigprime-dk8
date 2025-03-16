package com.bigprime.dk8.controller;

import cn.hutool.json.JSONUtil;
import com.bigprime.dk8.common.base.Result;
import com.bigprime.dk8.handler.model.ClusterConfigModel;
import com.bigprime.dk8.service.K8sService;
import com.bigprime.dk8.vo.NodeMetricsVo;
import com.bigprime.dk8.vo.PodMetricsVo;
import com.bigprime.dk8.vo.ReplicaSetStatusVo;
import com.bigprime.dk8.vo.WorkloadOverviewVo;
import io.kubernetes.client.openapi.models.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("kubernetes")
@Tag(name = "Kubernetes管理")
@RequiredArgsConstructor
public class K8sController {

    private final K8sService k8sService;

    //======================================================Cluster=====================================================
    @GetMapping("/cluster/list")
    @Operation(summary = "查询集群列表")
    public Result<List<ClusterConfigModel>> listClusters() {
        return Result.ok(k8sService.listClusters());
    }

    @PostMapping("/cluster/register")
    @Operation(summary = "注册或修改集群")
    public Result<Boolean> registerCluster(@RequestBody @Valid ClusterConfigModel clusterConfigModel) {
        return Result.ok(k8sService.registerCluster(clusterConfigModel));
    }

    @DeleteMapping("/cluster/{id}")
    @Operation(summary = "根据id删除集群")
    public Result<Boolean> removeCluster(@PathVariable("id") String id) {
        return Result.ok(k8sService.removeCluster(id));
    }

    @GetMapping("/cluster/switch/{id}")
    @Operation(summary = "根据id删除集群")
    public Result<Boolean> switchCluster(@PathVariable("id") String id) {
        return Result.ok(k8sService.switchCluster(id));
    }

    @GetMapping("/node/list")
    @Operation(summary = "获取节点列表")
    public V1NodeList listNodes() throws Exception {
        return k8sService.listNodes();
    }

    @GetMapping("/node/metrics")
    @Operation(summary = "获取节点监控")
    public List<NodeMetricsVo> getNodeMetrics() throws Exception {
        return k8sService.getNodeMetrics();
    }

    @PostMapping("/node/cordon")
    @Operation(summary = "节点Cordon操作")
    public String setNodeCordon(@RequestBody Map<String, String> params) {
        String nodeName = params.get("nodeName");
        return k8sService.nodeCordon(nodeName);
    }

    @PostMapping("/node/unCordon")
    @Operation(summary = "节点UnCordon操作")
    public String setNodeUnCordon(@RequestBody Map<String, String> params) {
        String nodeName = params.get("nodeName");
        return k8sService.nodeUnCordon(nodeName);
    }

    @PostMapping("/node/drain")
    @Operation(summary = "节点Drain操作")
    public String setNodeDrain(@RequestBody Map<String, String> params) {
        String nodeName = params.get("nodeName");
        return k8sService.nodeDrain(nodeName);
    }

    //======================================================Workloads===================================================
    @GetMapping("/namespace/list")
    @Operation(summary = "获取所有命名空间")
    public V1NamespaceList getNamespaces() throws Exception {
        return k8sService.getNamespaces();
    }

    @PostMapping("/namespace/create")
    @Operation(summary = "创建命名空间")
    public V1Namespace createNamespace(@RequestBody V1Namespace namespace) throws Exception {
        return k8sService.createNamespace(namespace);
    }

    @DeleteMapping("/namespace/{name}")
    @Operation(summary = "根据名称删除命名空间")
    public void deleteNamespace(@PathVariable("name") String name) throws Exception {
        k8sService.deleteNamespace(name);
    }

    @PostMapping("/pod/list")
    @Operation(summary = "获取Pod列表")
    public Result<String> listPod(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1PodList v1PodList = k8sService.listPods(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1PodList));
    }

    @PostMapping("/pod/metrics")
    @Operation(summary = "获取Pod指标")
    public Result<List<PodMetricsVo>> getPodMetrics(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return Result.ok(k8sService.getPodMetrics());
        } else {
            return Result.ok(k8sService.getPodMetrics(namespace));
        }
    }

    @PostMapping("/pod/logs")
    @Operation(summary = "获取Pod日志")
    public Result<String> getPodLogs(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.get("namespace");
        String pod = params.get("pod");
        return Result.ok(k8sService.getPodLogs(namespace, pod));
    }

    @PostMapping("/deployment/list")
    @Operation(summary = "获取Deployment列表")
    public Result<String> listDeployment(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1DeploymentList v1DeploymentList = k8sService.listDeployments(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1DeploymentList));
    }

    @PostMapping("/daemonSet/list")
    @Operation(summary = "获取daemonSet列表")
    public Result<String> listDaemonSet(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1DaemonSetList v1DaemonSetList = k8sService.listDaemonSets(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1DaemonSetList));
    }

    @PostMapping("/statefulSet/list")
    @Operation(summary = "获取statefulSet列表")
    public Result<String> listStatefulSet(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1StatefulSetList v1StatefulSetList = k8sService.listStatefulSets(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1StatefulSetList));
    }

    @PostMapping("/replicaSet/list")
    @Operation(summary = "获取replicaSet列表")
    public Result<String> listReplicaSet(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1ReplicaSetList v1ReplicaSetList = k8sService.listReplicaSets(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1ReplicaSetList));
    }

    @PostMapping("/replicaSet/status")
    @Operation(summary = "获取ReplicaSets的状态数据")
    public Result<List<ReplicaSetStatusVo>> getReplicaSetsStatus(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        if (namespace == null || namespace.isEmpty() || namespace.equals("--all")) {
            return Result.ok(k8sService.getReplicaSetStatus());
        } else {
            return Result.ok(k8sService.getReplicaSetStatus(namespace));
        }
    }


    @PostMapping("/replicationController/list")
    @Operation(summary = "获取replicationController列表")
    public Result<String> listReplicationController(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1ReplicationControllerList v1ReplicationControllerList = k8sService.listReplicationControllers(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1ReplicationControllerList));
    }

    @PostMapping("/job/list")
    @Operation(summary = "获取job列表")
    public Result<String> listJob(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1JobList v1JobList = k8sService.listJobs(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1JobList));
    }

    @PostMapping("/cronJob/list")
    @Operation(summary = "获取cronJob列表")
    public Result<String> listCronJob(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1CronJobList v1CronJobList = k8sService.listCronJobs(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1CronJobList));
    }

    //=================================================Config===========================================================
    @PostMapping("/configMap/list")
    @Operation(summary = "获取ConfigMap列表")
    public Result<String> listConfigMap(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1ConfigMapList v1ConfigMapList = k8sService.listConfigMaps(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1ConfigMapList));
    }

    @PostMapping("/secret/list")
    @Operation(summary = "获取Secret列表")
    public Result<String> listSecret(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1SecretList v1SecretList = k8sService.listSecrets(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1SecretList));
    }

    @PostMapping("/resourceQuota/list")
    @Operation(summary = "获取ResourceQuota列表")
    public Result<String> listResourceQuota(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1ResourceQuotaList v1ResourceQuotaList = k8sService.listResourceQuota(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1ResourceQuotaList));
    }

    @PostMapping("/limitRange/list")
    @Operation(summary = "获取LimitRange列表")
    public Result<String> listLimitRanges(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1LimitRangeList v1LimitRangeList = k8sService.listLimitRanges(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1LimitRangeList));
    }

    @PostMapping("/horizontalPodAutoscaler/list")
    @Operation(summary = "获取HorizontalPodAutoscaler列表")
    public Result<String> listHorizontalPodAutoscaler(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1HorizontalPodAutoscalerList v1HorizontalPodAutoscalerList = k8sService.listHorizontalPodAutoscalers(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1HorizontalPodAutoscalerList));
    }

    @PostMapping("/horizontalPodAutoscaler/list/v2")
    @Operation(summary = "获取HorizontalPodAutoscaler列表")
    public Result<String> listHorizontalPodAutoscalerV2(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V2HorizontalPodAutoscalerList v2HorizontalPodAutoscalerList = k8sService.listV2HorizontalPodAutoscalers(namespace);
        return Result.ok(JSONUtil.toJsonStr(v2HorizontalPodAutoscalerList));
    }

    @PostMapping("/podDisruptionBudget/list")
    @Operation(summary = "获取PodDisruptionBudget列表")
    public Result<String> listPodDisruptionBudget(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1PodDisruptionBudgetList v1PodDisruptionBudgetList = k8sService.listPodDisruptionBudgets(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1PodDisruptionBudgetList));
    }

    @GetMapping("/priorityClasses/list")
    @Operation(summary = "获取PriorityClasses列表")
    public Result<String> listPriorityClasses() throws Exception {
        V1PriorityClassList v1PriorityClassList = k8sService.listPriorityClasses();
        return Result.ok(JSONUtil.toJsonStr(v1PriorityClassList));
    }

    @GetMapping("/runtimeClasses/list")
    @Operation(summary = "获取RuntimeClass列表")
    public Result<String> listRuntimeClasses() throws Exception {
        V1RuntimeClassList v1RuntimeClassList = k8sService.listRuntimeClasses();
        return Result.ok(JSONUtil.toJsonStr(v1RuntimeClassList));
    }

    @PostMapping("/leases/list")
    @Operation(summary = "获取Leases列表")
    public Result<String> listLeases(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1LeaseList v1LeaseList = k8sService.listLeases(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1LeaseList));
    }

    @GetMapping("/mutatingWebhookConfiguration/list")
    @Operation(summary = "获取MutatingWebhookConfiguration列表")
    public Result<String> listMutatingWebhookConfigurations() throws Exception {
        V1MutatingWebhookConfigurationList v1MutatingWebhookConfigurationList = k8sService.listMutatingWebhookConfigurations();
        return Result.ok(JSONUtil.toJsonStr(v1MutatingWebhookConfigurationList));
    }

    @GetMapping("/validatingWebhookConfiguration/list")
    @Operation(summary = "获取ValidatingWebhookConfiguration列表")
    public Result<String> listValidatingWebhookConfiguration() throws Exception {
        V1ValidatingWebhookConfigurationList v1ValidatingWebhookConfigurationList = k8sService.listValidatingWebhookConfigurations();
        return Result.ok(JSONUtil.toJsonStr(v1ValidatingWebhookConfigurationList));
    }

    //======================================================NetWork=====================================================
    @PostMapping("/service/list")
    @Operation(summary = "获取Service列表")
    public Result<String> listServices(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1ServiceList v1ServiceList = k8sService.listServices(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1ServiceList));
    }

    @PostMapping("/endpoint/list")
    @Operation(summary = "获取Endpoint列表")
    public Result<String> listEndpoints(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1EndpointsList v1EndpointsList = k8sService.listEndpoints(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1EndpointsList));
    }

    @PostMapping("/networkPolicy/list")
    @Operation(summary = "获取NetworkPolicy列表")
    public Result<String> listNetworkPolicies(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1NetworkPolicyList v1NetworkPolicyList = k8sService.listNetworkPolicies(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1NetworkPolicyList));
    }

    @PostMapping("/ingress/list")
    @Operation(summary = "获取NetworkPolicy列表")
    public Result<String> listIngress(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1IngressList v1IngressList = k8sService.listIngress(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1IngressList));
    }

    @GetMapping("/ingressClass/list")
    @Operation(summary = "获取IngressClass列表")
    public Result<String> listIngressClasses() throws Exception {
        V1IngressClassList v1IngressClassList = k8sService.listIngressClasses();
        return Result.ok(JSONUtil.toJsonStr(v1IngressClassList));
    }

    //=================================================Storage==========================================================

    @PostMapping("/persistentVolumeClaim/list")
    @Operation(summary = "获取PersistentVolumeClaim列表")
    public Result<String> listPersistentVolumeClaims(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1PersistentVolumeClaimList v1PersistentVolumeClaimList = k8sService.listPersistentVolumeClaims(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1PersistentVolumeClaimList));
    }

    @PostMapping("/persistentVolumeClaim/pods")
    @Operation(summary = "获取pod申请pvc列表")
    public Result<Map<String, String>> getPodUsePVC(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        Map<String, String> pvcUsePods = k8sService.getPVCUsePods(namespace);
        return Result.ok(pvcUsePods);
    }

    @GetMapping("/persistentVolume/list")
    @Operation(summary = "获取PersistentVolume列表")
    public Result<String> listPersistentVolumeList() throws Exception {
        V1PersistentVolumeList v1PersistentVolumeList = k8sService.listPersistentVolumeList();
        return Result.ok(JSONUtil.toJsonStr(v1PersistentVolumeList));
    }

    @GetMapping("/storageClass/list")
    @Operation(summary = "获取StorageClass列表")
    public Result<String> listStorageClasses() throws Exception {
        V1StorageClassList v1StorageClassList = k8sService.listStorageClasses();
        return Result.ok(JSONUtil.toJsonStr(v1StorageClassList));
    }

    //====================================================AccessControl=================================================

    @PostMapping("/serviceAccount/list")
    @Operation(summary = "获取ServiceAccount列表")
    public Result<String> listServiceAccounts(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1ServiceAccountList v1ServiceAccountList = k8sService.listServiceAccounts(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1ServiceAccountList));
    }

    @GetMapping("/clusterRole/list")
    @Operation(summary = "获取ClusterRole列表")
    public Result<String> listClusterRoles() throws Exception {
        V1ClusterRoleList v1ClusterRoleList = k8sService.listClusterRoles();
        return Result.ok(JSONUtil.toJsonStr(v1ClusterRoleList));
    }

    @PostMapping("/role/list")
    @Operation(summary = "获取Role列表")
    public Result<String> listRoles(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1RoleList v1RoleList = k8sService.listRoles(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1RoleList));
    }

    @GetMapping("/clusterRoleBinding/list")
    @Operation(summary = "获取ClusterRoleBinding列表")
    public Result<String> listClusterRoleBindings() throws Exception {
        V1ClusterRoleBindingList v1ClusterRoleBindingList = k8sService.listClusterRoleBindings();
        return Result.ok(JSONUtil.toJsonStr(v1ClusterRoleBindingList));
    }

    @PostMapping("/roleBinding/list")
    @Operation(summary = "获取RoleBinding列表")
    public Result<String> listRoleBindings(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        V1RoleBindingList v1RoleBindingList = k8sService.listRoleBindings(namespace);
        return Result.ok(JSONUtil.toJsonStr(v1RoleBindingList));
    }

    @PostMapping("/event/list")
    @Operation(summary = "获取Event列表")
    public Result<String> listEvents(@RequestBody Map<String, String> params) throws Exception {
        String namespace = params.getOrDefault("namespace", "--all");
        CoreV1EventList coreV1EventList = k8sService.listEvents(namespace);
        return Result.ok(JSONUtil.toJsonStr(coreV1EventList));
    }

    //==================================================overview========================================================
    @GetMapping("/workload/overview")
    @Operation(summary = "获取workload图表数据")
    public Result<List<WorkloadOverviewVo>> getWorkloadOverview() throws Exception {
        List<WorkloadOverviewVo> workloadOverview = k8sService.getWorkloadOverview();
        return Result.ok(workloadOverview);
    }

    @PostMapping("/resource/patch/update")
    @Operation(summary = "局部更新资源")
    public Result<String> patchUpdateResource(@RequestBody Map<String, String> params) {
        String namespace = params.get("namespace");
        String resType = params.get("resType");
        String resName = params.get("resName");
        String patchContent = params.get("patchContent");
        String s = k8sService.patchUpdateResource(namespace, resType, resName, patchContent);
        return Result.ok(s);
    }

    @PostMapping("/resource/create")
    @Operation(summary = "创建资源")
    public Result<String> createResource(@RequestBody Map<String, String> params) {
        String resType = params.get("resType");
        String yaml = params.get("yaml");
        String s = k8sService.createResource(resType, yaml);
        return Result.ok(s);
    }

    @PostMapping("/resource/delete")
    @Operation(summary = "删除资源")
    public Result<String> deleteResource(@RequestBody Map<String, String> params) {
        String namespace = params.get("namespace");
        String resType = params.get("resType");
        String resName = params.get("resName");
        String s = k8sService.deleteResource(resType, resName, namespace);
        return Result.ok(s);
    }

    @PostMapping("/resource/logs")
    @Operation(summary = "资源日志")
    public Result<String> getResourceLogs(@RequestBody Map<String, String> params) {
        String namespace = params.get("namespace");
        String resType = params.get("resType");
        String resName = params.get("resName");
        String s = k8sService.getResourceLogs(resType, resName, namespace);
        return Result.ok(s);
    }

    @PostMapping("/resource/update")
    @Operation(summary = "资源更新")
    public Result<String> updateResource(@RequestBody Map<String, String> params) {
        String resType = params.get("resType");
        String resName = params.get("resName");
        String namespace = params.get("namespace");
        String content = params.get("content");
        String s = k8sService.updateResource(resType, resName, namespace, content);
        return Result.ok(s);
    }
}
