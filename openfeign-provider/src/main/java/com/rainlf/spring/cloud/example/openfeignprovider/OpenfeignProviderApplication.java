package com.rainlf.spring.cloud.example.openfeignprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class OpenfeignProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenfeignProviderApplication.class, args);
    }

}
