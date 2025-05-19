package com.example.loanbroker;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TopicConsumer extends RouteBuilder {
    @Override
    public void configure() {
        from("activemq:topic:creditTopic")
                .log("Second topic consumer ${routeId} received SSN: ${body}")
                .transform(simple("${body.hashCode() % 1000}"))
                .log("Second topic consumer ${routeId} credit score: ${body}");
    }
}