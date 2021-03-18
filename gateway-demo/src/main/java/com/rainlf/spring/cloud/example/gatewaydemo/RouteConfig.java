package com.rainlf.spring.cloud.example.gatewaydemo;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : rain
 * @date : 2021/3/18 19:11
 */
@Configuration
public class RouteConfig {
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 匹配路径
                .route(r -> r.path("/")
                        // 转发路由
                        .uri("http://www.baidu.com")
                        // 注册自定义过滤器
                        .filters(new MyFilter())
                        // 给定id
                        .id("my-route"))
                .build();
    }
}
