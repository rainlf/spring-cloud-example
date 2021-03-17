package com.rainlf.spring.cloud.example.zipkinclienta;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author : rain
 * @date : 2021/3/17 17:02
 */
@Slf4j
@RestController
public class AController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public String test() {
        log.info("this is a");
        return restTemplate.getForObject("http://zipkin-client-b", String.class);
    }
}
