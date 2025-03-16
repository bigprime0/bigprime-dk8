package com.bigprime.dk8.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplicaSetStatusVo {
    private String name;
    private int desired;
    private int current;
    private int ready;
}
