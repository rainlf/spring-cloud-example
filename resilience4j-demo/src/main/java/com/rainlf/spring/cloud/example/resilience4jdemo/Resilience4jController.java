package com.rainlf.spring.cloud.example.resilience4jdemo;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : rain
 * @date : 2021/3/17 10:26
 */
@Slf4j
@RestController
public class Resilience4jController {
    private int i = 0;

    @RateLimiter(name = "testRateLimiter", fallbackMethod = "fallbackMethod")
    @GetMapping("testRateLimiter")
    public String testRateLimiter(){
        return "ok " + i++;
    }

    @Bulkhead(name = "testBulkhead", fallbackMethod = "fallbackMethod")
    @GetMapping("testBulkhead")
    public String testBulkhead(){
        return "ok " + i++;
    }

    @Bulkhead(name = "testThreadPoolBulkhead", fallbackMethod = "fallbackMethod", type = Bulkhead.Type.THREADPOOL)
    @GetMapping("testThreadPoolBulkhead")
    public String testThreadPoolBulkhead(){
        return "ok " + i++;
    }


    @CircuitBreaker(name = "testCircuitBreaker", fallbackMethod = "fallbackMethod")
    @GetMapping("testCircuitBreaker")
    public String testCircuitBreaker(){
        if (i++ % 2 == 0) {
            throw new RuntimeException("random exception");
        }
        return "ok " + i++;
    }

    @Retry(name = "testRetry", fallbackMethod = "fallbackMethod")
    @GetMapping("testRetry")
    public String testRetry(){
        if (true) {
            throw new RuntimeException("exception");
        }
        return "ok " + i++;
    }

    public String fallbackMethod(Throwable throwable) {
        log.error("Fallback Happened", throwable);
        return "ok fallback";
    }

    @Autowired
    BulkheadRegistry bulkheadRegistry;
    @Autowired
    ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry;
    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;
    @Autowired
    RateLimiterRegistry rateLimiterRegistry;
    @Autowired
    RetryRegistry retryRegistry;

    @GetMapping("check")
    public Map<String, Number> check() {
        Map<String, Number> result = new HashMap<>();
        result.put("Bulkhead maxConcurrentCalls", bulkheadRegistry.getDefaultConfig().getMaxConcurrentCalls());
        result.put("CircuitBreaker failureRateThreshold", circuitBreakerRegistry.getDefaultConfig().getFailureRateThreshold());
        result.put("RateLimiter limitForPeriod", rateLimiterRegistry.getDefaultConfig().getLimitForPeriod());
        result.put("ThreadPoolBulkhead max thread pool", threadPoolBulkheadRegistry.getDefaultConfig().getMaxThreadPoolSize());
        result.put("Retry max retry", retryRegistry.getDefaultConfig().getMaxAttempts());
        return result;
    }
}
