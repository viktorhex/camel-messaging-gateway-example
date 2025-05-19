package com.example.loanbroker;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageGatewayImpl implements MessageGateway {
    @Produce("activemq:queue:creditRequest")
    private ProducerTemplate syncProducer;

    @Produce("activemq:queue:asyncCreditRequest")
    private ProducerTemplate asyncProducer;

    @Produce("activemq:topic:creditTopic")
    private ProducerTemplate topicProducer;

    @Produce("activemq:queue:filteredCreditRequest")
    private ProducerTemplate filteredProducer;

    @Produce("activemq:queue:idempotentCreditRequest")
    private ProducerTemplate idempotentProducer;

    @Produce("activemq:queue:asyncServiceRequest")
    private ProducerTemplate asyncServiceProducer;

    @Override
    public String sendCreditRequest(String input) {
        return syncProducer.requestBody("activemq:queue:creditRequest", input, String.class);
    }

    @Override
    public void sendAsyncCreditRequest(String input) {
        asyncProducer.sendBody("activemq:queue:asyncCreditRequest", input);
    }

    @Override
    public void sendTopicCreditRequest(String input) {
        topicProducer.sendBody("activemq:topic:creditTopic", input);
    }

    @Override
    public void sendFilteredCreditRequest(String input) {
        filteredProducer.sendBody("activemq:queue:filteredCreditRequest", input);
    }

    @Override
    public void sendIdempotentCreditRequest(String input, String messageId) {
        idempotentProducer.sendBodyAndHeader("activemq:queue:idempotentCreditRequest", input, "messageId", messageId);
    }

    @Override
    public void sendAsyncServiceRequest(String input) {
        asyncServiceProducer.sendBody("activemq:queue:asyncServiceRequest", input);
    }
}