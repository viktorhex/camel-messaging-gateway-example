package com.example.loanbroker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreditBureauGatewayImpl implements CreditBureauGateway {
    private final MessageGateway messageGateway;

    @Autowired
    public CreditBureauGatewayImpl(MessageGateway messageGateway) {
        this.messageGateway = messageGateway;
    }

    @Override
    public int getCreditScore(String ssn) {
        String response = messageGateway.sendCreditRequest(ssn);
        try {
            return Integer.parseInt(response);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid credit score response: " + response, e);
        }
    }

    @Override
    public void requestAsyncCreditScore(String ssn) {
        messageGateway.sendAsyncCreditRequest(ssn);
    }

    @Override
    public void requestTopicCreditScore(String ssn) {
        messageGateway.sendTopicCreditRequest(ssn);
    }

    @Override
    public void requestFilteredCreditScore(String ssn) {
        messageGateway.sendFilteredCreditRequest(ssn);
    }

    @Override
    public void requestIdempotentCreditScore(String ssn, String messageId) {
        messageGateway.sendIdempotentCreditRequest(ssn, messageId);
    }

    @Override
    public void requestAsyncServiceCreditScore(String ssn) {
        messageGateway.sendAsyncServiceRequest(ssn);
    }
}