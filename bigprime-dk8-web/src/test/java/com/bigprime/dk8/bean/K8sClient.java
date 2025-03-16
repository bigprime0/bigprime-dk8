package com.bigprime.dk8.bean;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

public class K8sClient {

    public ApiClient apiClient() throws Exception {

        ApiClient client = Config.fromConfig("D:\\BigDataCode\\New\\bigprime-dk8\\bigprime-dk8-web\\src\\main\\resources\\config");
        client.setVerifyingSsl(false);
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        System.out.println("Connected to Kubernetes API server: " + client.getBasePath());
        return client;
    }

    public CoreV1Api coreV1Api() throws Exception {
        return new CoreV1Api(apiClient());
    }
}
