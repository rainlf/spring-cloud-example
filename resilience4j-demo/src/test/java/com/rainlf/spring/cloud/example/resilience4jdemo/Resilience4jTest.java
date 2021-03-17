package com.rainlf.spring.cloud.example.resilience4jdemo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;

/**
 * @author : rain
 * @date : 2021/3/17 14:20
 */
@Slf4j
@SpringBootTest
class Resilience4jTest {

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void testRateLimiter() {
        batchRequest("http://localhost:8030/testRateLimiter", 10);
    }

    @Test
    void testBulkhead() {
        batchRequest("http://localhost:8030/testBulkhead", 10);
    }

    @Test
    void testThreadPoolBulkhead() {
        batchRequest("http://localhost:8030/testThreadPoolBulkhead", 10);
    }

    @Test
    void testCircuitBreaker() {
        batchRequest("http://localhost:8030/testCircuitBreaker", 10);
    }

    @Test
    void testRetry() {
        batchRequest("http://localhost:8030/testRetry", 1);
    }

    private void batchRequest(String url, int batchNum) {
        CountDownLatch endLatch = new CountDownLatch(batchNum);

        CountDownLatch countDownLatch = new CountDownLatch(batchNum);
        for (int i = 0; i < batchNum; i++) {
            new Thread(() -> {
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String re = restTemplate.getForObject(url, String.class);
                log.info("re: {}", re);
                endLatch.countDown();
            }).start();
        }

        try {
            endLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}