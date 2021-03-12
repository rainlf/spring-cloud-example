package com.rainlf.spring.cloud.example.openfeignprovider;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author : rain
 * @date : 2021/3/12 14:27
 */
@RestController
public class OpenFeignProviderController {

    @GetMapping("")
    public String sayHi() {
        return "hi, this is from open feign provider";
    }

    @GetMapping("fail")
    public String sayHiWithFail() {
        throw new RuntimeException("hi, this is from open feign provider with fail");
    }

    @GetMapping("sleep")
    public String sayHiWithSleep() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        return "hi, this is from open feign provider with sleep 10s";
    }
}
