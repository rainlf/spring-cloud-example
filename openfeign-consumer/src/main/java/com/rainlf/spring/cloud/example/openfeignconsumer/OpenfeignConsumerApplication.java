package com.rainlf.spring.cloud.example.openfeignconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class OpenfeignConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenfeignConsumerApplication.class, args);
    }

}
