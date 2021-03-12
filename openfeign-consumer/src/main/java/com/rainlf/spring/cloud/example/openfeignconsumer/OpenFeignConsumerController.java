package com.rainlf.spring.cloud.example.openfeignconsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : rain
 * @date : 2021/3/12 14:47
 */
@RestController
public class OpenFeignConsumerController {

    @Autowired
    private IOpenFeignProvider openFeignProvider;

    @GetMapping("")
    public String sayHi() {
        return openFeignProvider.sayHi();
    }

    @GetMapping("fail")
    public String sayHiWithFail() {
        return openFeignProvider.sayHiWithFail();
    }

    @GetMapping("sleep")
    public String sayHiWithSleep() {
        return openFeignProvider.sayHiWithSleep();
    }
}
