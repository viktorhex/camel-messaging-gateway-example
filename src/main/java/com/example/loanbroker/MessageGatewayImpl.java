package com.example.loanbroker;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageGatewayImpl implements MessageGateway {
    @Produce("activemq:queue:creditRequest")
    private ProducerTemplate producer;

    @Override
    public String sendCreditRequest(String ssn) {
        return producer.requestBody("activemq:queue:creditRequest", ssn, String.class);
    }
}