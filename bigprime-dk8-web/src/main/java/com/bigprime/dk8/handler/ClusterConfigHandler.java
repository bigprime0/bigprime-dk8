package com.bigprime.dk8.handler;

import com.bigprime.das.core.config.BigprimeEntityQuery;
import com.bigprime.dk8.common.base.SecurityUser;
import com.bigprime.dk8.common.config.kubernetes.K8sContextHolder;
import com.bigprime.dk8.common.utils.IdUtils;
import com.bigprime.dk8.handler.model.ClusterConfigModel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Data
@RequiredArgsConstructor
public class ClusterConfigHandler {
    private final ConcurrentMap<String, String> configCache = new ConcurrentHashMap<>();
    private final BigprimeEntityQuery proxy;

    public void InitConfigCache() {
        configCache.clear();
        List<ClusterConfigModel> configs = list();
        for (ClusterConfigModel config : configs) {
            configCache.put(config.getClusterId(), config.getConfigContent());
        }
    }

    public String getConfig() {
        if (configCache.isEmpty()) {
            InitConfigCache();
        }
        return configCache.get(K8sContextHolder.getCurrentClusterId());
    }

    public boolean add(ClusterConfigModel model) {
        ClusterConfigModel byName = getByName(model.getClusterName());
        if(byName == null) {
            if (model.getClusterId().isEmpty()) {
                model.setClusterId(IdUtils.getUUId(true));
                model.setCreateUser(SecurityUser.getUser().getId().toString());
                model.setCreateTime(new Date());
                long rows = proxy.insertable(model).executeRows(true);
                if (rows > 0) {
                    configCache.put(model.getClusterId(), model.getConfigContent());
                    return true;
                }
            } else {
                long rows = proxy.updatable(model).executeRows();
                if (rows > 0) {
                    configCache.remove(model.getClusterId());
                    configCache.put(model.getClusterId(), model.getConfigContent());
                    return true;
                }
            }
        }else{
            return false;
        }
        return false;
    }

    public ClusterConfigModel get(String clusterId) {
        return proxy.queryable(ClusterConfigModel.class)
                .where(i -> i.clusterId().eq(clusterId))
                .firstOrNull();
    }

    public ClusterConfigModel getByName(String clusterName) {
        return proxy.queryable(ClusterConfigModel.class)
                .where(i -> i.clusterName().eq(clusterName))
                .firstOrNull();
    }

    public List<ClusterConfigModel> list() {
        return proxy.queryable(ClusterConfigModel.class).toList();
    }

    public boolean remove(String clusterId) {
        ClusterConfigModel model = get(clusterId);
        long rows = proxy.deletable(model)
                .allowDeleteStatement(true)
                .executeRows();
        if (rows > 0) {
            configCache.remove(model.getClusterId());
            return true;
        }
        return false;
    }
}
