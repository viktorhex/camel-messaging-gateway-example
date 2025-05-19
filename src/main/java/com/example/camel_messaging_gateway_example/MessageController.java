package com.example.camel_messaging_gateway_example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {
    @Autowired
    private UppercaseGateway uppercaseGateway;

    @GetMapping("/convert")
    public String convert(@RequestParam String text) {
        return uppercaseGateway.sendToUppercaseQueue(text);
    }
}