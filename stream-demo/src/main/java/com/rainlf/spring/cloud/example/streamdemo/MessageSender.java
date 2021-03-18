package com.rainlf.spring.cloud.example.streamdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author : rain
 * @date : 2021/3/18 10:54
 */
@RestController
public class MessageSender {
    @Autowired
    private MessageTopic messageTopic;

    @GetMapping("")
    public String sendMessage() {
        messageTopic.messageOut().send(MessageBuilder.withPayload("date is " + new Date()).build());
        return "ok";
    }
}
