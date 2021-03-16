package com.rainlf.spring.cloud.example.openfeignconsumer.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author : rain
 * @date : 2021/3/12 14:48
 */
@FeignClient(value = "${openfeign.provider.name}", fallback = OpenFeignProviderFallback.class)
public interface IOpenFeignProvider {
    @GetMapping("")
    String sayHi();
    
    @GetMapping("fail")
    String sayHiWithFail();

    @GetMapping("sleep")
    String sayHiWithSleep();
}
