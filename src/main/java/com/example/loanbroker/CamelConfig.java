package com.example.loanbroker;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
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
                                .transform(simple("${body.hashCode() % 1000}"))
                                .log("Credit score: ${body}");
            }
        };
    }

    @Bean
    public RouteBuilder asyncCreditRequestRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("activemq:queue:asyncCreditRequest")
                        .log("Async credit request for SSN: ${body}")
                        .transform(simple("${body.hashCode() % 1000}"))
                        .log("Async credit score: ${body}")
                        .to("activemq:queue:asyncResults");
            }
        };
    }

    @Bean
    public RouteBuilder topicCreditRequestRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("activemq:topic:creditTopic")
                        .log("Topic consumer ${routeId} received SSN: ${body}")
                        .transform(simple("${body.hashCode() % 1000}"))
                        .log("Topic consumer ${routeId} credit score: ${body}");
            }
        };
    }

    @Bean
    public RouteBuilder filteredCreditRequestRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("activemq:queue:filteredCreditRequest")
                        .filter(simple("${body.length()} == 9"))
                        .log("Filtered SSN: ${body}")
                        .transform(simple("${body.hashCode() % 1000}"))
                        .log("Filtered credit score: ${body}")
                        .to("activemq:queue:filteredResults");
            }
        };
    }

    @Bean
    public RouteBuilder idempotentCreditRequestRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("activemq:queue:idempotentCreditRequest")
                        .idempotentConsumer(header("messageId"), new MemoryIdempotentRepository())
                        .log("Idempotent SSN: ${body}")
                        .transform(simple("${body.hashCode() % 1000}"))
                        .log("Idempotent credit score: ${body}")
                        .to("activemq:queue:idempotentResults");
            }
        };
    }

    @Bean
    public RouteBuilder asyncServiceRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("activemq:queue:asyncServiceRequest")
                        .log("Async service SSN: ${body}")
                        .transform(simple("${body.hashCode() % 1000}"))
                        .to("activemq:queue:serviceResults")
                        .log("Async service result sent: ${body}");
            }
        };
    }
}