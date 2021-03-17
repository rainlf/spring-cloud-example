package com.rainlf.spring.cloud.example.zipkinclientb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : rain
 * @date : 2021/3/17 17:02
 */
@Slf4j
@RestController
public class BController {

    @GetMapping
    public String test() {
      log.info("this is b");
      return "ok";
    }
}
