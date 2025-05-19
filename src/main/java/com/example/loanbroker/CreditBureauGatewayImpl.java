package com.example.loanbroker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreditBureauGatewayImpl implements CreditBureauGateway {
    @Autowired
    private MessageGateway messageGateway;

    @Override
    public int getCreditScore(String ssn) {
        String response = messageGateway.sendCreditRequest(ssn);
        return Integer.parseInt(response); // Simplified parsing
    }
}