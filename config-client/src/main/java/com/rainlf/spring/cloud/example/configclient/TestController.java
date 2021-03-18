package com.rainlf.spring.cloud.example.configclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : rain
 * @date : 2021/3/18 13:43
 */
@Slf4j
@RefreshScope
@RestController
public class TestController {
    @Value("${test.value1}")
    private Integer value1;

    @Value("${test.value2}")
    private String value2;

    @GetMapping("")
    public String test() {
        log.info("value1: {}", value1);
        log.info("value2: {}", value2);
        return value1 + " " + value2;
    }
}
