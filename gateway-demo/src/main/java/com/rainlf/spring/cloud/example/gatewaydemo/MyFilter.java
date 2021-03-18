package com.rainlf.spring.cloud.example.gatewaydemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author : rain
 * @date : 2021/3/18 19:10
 */
@Slf4j
public class MyFilter implements GatewayFilter, Ordered {
    private static final String START_TIME = "start_time";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(START_TIME);
                    if (startTime != null) {
                        log.info("{}: {} ms", exchange.getRequest().getURI().getRawPath(), (System.currentTimeMillis() - startTime));
                    }
                })
        );
    }

    /*
     *过滤器存在优先级，order越小，优先级越高
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
