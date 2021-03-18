package com.rainlf.spring.cloud.example.streamdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

/**
 * @author : rain
 * @date : 2021/3/18 11:05
 */
@Slf4j
@Component
public class MessageListener {
    @StreamListener(value = MessageTopic.MESSAGE_IN)
    public void receive(String payload) {
        log.info("receive: " + payload);
    }
}
