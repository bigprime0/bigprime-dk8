package com.bigprime.dk8;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Phoenix
 * @version 1.0
 */
@EnableAsync
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = "com.bigprime")
public class Dk8Application {
    public static void main(String[] args) {
        SpringApplication.run(Dk8Application.class, args);
    }
}
