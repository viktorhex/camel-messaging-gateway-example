package com.example.loanbroker;

public interface MessageGateway {
    String sendCreditRequest(String input);
    void sendAsyncCreditRequest(String input);
    void sendTopicCreditRequest(String input);
    void sendFilteredCreditRequest(String input);
    void sendIdempotentCreditRequest(String input, String messageId);
    void sendAsyncServiceRequest(String input);
}