package com.example.loanbroker;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {
    @Bean
    public RouteBuilder creditRequestRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                onException(IllegalArgumentException.class)
                        .handled(true)
                        .transform(constant("Error: Invalid input"));

                from("activemq:queue:creditRequest")
                        .log("Received SSN: ${body}")
                        .choice()
                            .when(simple("${body} == null"))
                                .throwException(new IllegalArgumentException("Input cannot be null"))
                            .otherwise()
                                .transform(simple("${body.hashCode() % 1000}")) // Mock credit score
                                .log("Credit score: ${body}");
            }
        };
    }
}