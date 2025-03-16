package com.bigprime.dk8.handler.model;

import com.bigprime.dk8.handler.model.proxy.ClusterConfigModelProxy;
import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@EntityProxy
@Table("t_k8s_cluster")
@AllArgsConstructor
@NoArgsConstructor
public class ClusterConfigModel implements ProxyEntityAvailable<ClusterConfigModel, ClusterConfigModelProxy> {

    /**
     * 集群编号
     */
    @Column(primaryKey = true)
    private String clusterId;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * k8s admin Config
     */
    private String configContent;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

}
