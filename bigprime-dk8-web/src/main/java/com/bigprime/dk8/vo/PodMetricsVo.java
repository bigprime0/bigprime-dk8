package com.bigprime.dk8.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PodMetricsVo {
    private String podName;
    private String namespace;
    private List<ContainerMetricsVo> containerMetrics;
}
