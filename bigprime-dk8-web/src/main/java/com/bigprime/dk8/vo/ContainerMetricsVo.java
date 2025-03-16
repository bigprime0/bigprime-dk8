package com.bigprime.dk8.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ContainerMetricsVo {
    private String name;
    private BigDecimal cpu;
    private BigDecimal memory;
}
