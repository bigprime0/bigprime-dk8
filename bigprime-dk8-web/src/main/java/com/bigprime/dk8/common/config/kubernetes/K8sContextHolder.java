package com.bigprime.dk8.common.config.kubernetes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class K8sContextHolder {
    //private static final ThreadLocal<String> currentCluster = new ThreadLocal<>();
    private static final ConcurrentMap<String, String> currentCluster = new ConcurrentHashMap<>();

    // 设置当前集群上下文
    public static void setCurrentClusterId(String clusterId) {
        currentCluster.put("ClusterId", clusterId);
    }

    // 获取当前集群ID
    public static String getCurrentClusterId() {
        return currentCluster.get("ClusterId");
    }

    // 清除上下文
    public static void clear() {
        currentCluster.remove("ClusterId");
    }
}
