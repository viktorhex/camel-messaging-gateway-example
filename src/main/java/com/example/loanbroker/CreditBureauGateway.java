package com.example.loanbroker;

public interface CreditBureauGateway {
    int getCreditScore(String ssn);
    void requestAsyncCreditScore(String ssn);
    void requestTopicCreditScore(String ssn);
    void requestFilteredCreditScore(String ssn);
    void requestIdempotentCreditScore(String ssn, String messageId);
    void requestAsyncServiceCreditScore(String ssn);
}