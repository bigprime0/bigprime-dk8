package com.bigprime.dk8.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkloadOverviewVo {
    private String workload;
    private int runningCount;
    private int unRunningCount;
}
