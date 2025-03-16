package com.bigprime.dk8.common.config.kubernetes;

import com.bigprime.dk8.handler.ClusterConfigHandler;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class K8sClientProvider {
    private final ClusterConfigHandler configStorage;
    private static final ConcurrentMap<String, ApiClient> currentClient = new ConcurrentHashMap<>();

    // 使用io.kubernetes动态创建API客户端
    public ApiClient apiClient() throws Exception {
        ApiClient client = null;
        String currentClusterId = K8sContextHolder.getCurrentClusterId();
        if (currentClusterId != null) {
            if (!currentClient.isEmpty()) {
                client = currentClient.get(currentClusterId);
            }
        }
        if (client == null) {
            String kubeConfig = configStorage.getConfig();
            if (kubeConfig != null && !kubeConfig.isEmpty()) {
                client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new StringReader(kubeConfig))).build();
                client.setVerifyingSsl(false);
                io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
                if (currentClusterId != null) {
                    currentClient.put(currentClusterId, client);
                }
                return client;
            }
        }
        return client;
    }


    public CoreV1Api coreV1Api() throws Exception {
        return new CoreV1Api(apiClient());
    }


    public Metrics metricsApi() throws Exception {
        return new Metrics(apiClient());
    }


    public AppsV1Api appsV1Api() throws Exception {
        return new AppsV1Api(apiClient());
    }


    public BatchV1Api batchV1Api() throws Exception {
        return new BatchV1Api(apiClient());
    }


    public AutoscalingV1Api autoscalingV1Api() throws Exception {
        return new AutoscalingV1Api(apiClient());
    }


    public AutoscalingV2Api autoscalingV2Api() throws Exception {
        return new AutoscalingV2Api(apiClient());
    }


    public PolicyV1Api policyV1Api() throws Exception {
        return new PolicyV1Api(apiClient());
    }


    public SchedulingV1Api schedulingV1Api() throws Exception {
        return new SchedulingV1Api(apiClient());
    }


    public NodeV1Api nodeV1Api() throws Exception {
        return new NodeV1Api(apiClient());
    }


    public NetworkingV1Api networkingV1Api() throws Exception {
        return new NetworkingV1Api(apiClient());
    }


    public RbacAuthorizationV1Api rbacAuthorizationV1Api() throws Exception {
        return new RbacAuthorizationV1Api(apiClient());
    }


    public CoordinationV1Api coordinationV1Api() throws Exception {
        return new CoordinationV1Api(apiClient());
    }


    public AdmissionregistrationV1Api admissionregistrationV1Api() throws Exception {
        return new AdmissionregistrationV1Api(apiClient());
    }


    public StorageV1Api storageV1Api() throws Exception {
        return new StorageV1Api(apiClient());
    }
}
