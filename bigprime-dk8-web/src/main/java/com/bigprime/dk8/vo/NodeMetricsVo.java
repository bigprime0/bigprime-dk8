package com.bigprime.dk8.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class NodeMetricsVo {
    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * cpu使用
     */
    private BigDecimal cpu;

    /**
     * 内存使用
     */
    private BigDecimal memory;
}

