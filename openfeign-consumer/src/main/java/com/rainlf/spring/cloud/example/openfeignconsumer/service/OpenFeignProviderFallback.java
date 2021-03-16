package com.rainlf.spring.cloud.example.openfeignconsumer.service;

import org.springframework.stereotype.Component;

/**
 * @author : rain
 * @date : 2021/3/12 16:00
 */
@Component
public class OpenFeignProviderFallback implements IOpenFeignProvider{
    @Override
    public String sayHi() {
        return "hi, this is from open feign customer call back, sayHi";
    }

    @Override
    public String sayHiWithFail() {
        return "hi, this is from open feign customer call back, sayHiWithFail";
    }

    @Override
    public String sayHiWithSleep() {
        return "hi, this is from open feign customer call back, sayHiWithSleep";
    }
}
