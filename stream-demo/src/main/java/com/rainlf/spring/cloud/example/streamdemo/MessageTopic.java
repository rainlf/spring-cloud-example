package com.rainlf.spring.cloud.example.streamdemo;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author : rain
 * @date : 2021/3/18 10:53
 */
public interface MessageTopic {
    String MESSAGE_OUT = "message-out";
    String MESSAGE_IN = "message-in";

    @Output(MESSAGE_OUT)
    MessageChannel messageOut();

    @Input(MESSAGE_IN)
    SubscribableChannel messageIn();
}
