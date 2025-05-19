package com.example.camel_messaging_gateway_example;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {
    @Bean
    public RouteBuilder uppercaseRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("activemq:queue:input")
                        .log("Received from queue: ${body}")
                        .transform(simple("${body.toUpperCase()}"))
                        .log("Transformed: ${body}");
            }
        };
    }

    static class GatewayImpl implements UppercaseGateway {
        @Produce("activemq:queue:input")
        private ProducerTemplate producer;

        @Override
        public String sendToUppercaseQueue(String input) {
            return producer.requestBody("activemq:queue:input", input, String.class);
        }
    }

    @Bean
    public UppercaseGateway uppercaseGateway() {
        return new GatewayImpl();
    }
}