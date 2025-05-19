package com.example.loanbroker;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class DurableSubscriber extends RouteBuilder {
    @Override
    public void configure() {
        from("activemq:topic:creditTopic?clientId=testClient&subscriptionName=durableSub")
                .log("Durable subscriber received SSN: ${body}")
                .transform(simple("${body.hashCode() % 1000}"))
                .log("Durable subscriber credit score: ${body}");
    }
}