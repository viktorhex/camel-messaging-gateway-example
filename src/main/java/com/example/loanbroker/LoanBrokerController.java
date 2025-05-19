package com.example.loanbroker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoanBrokerController {
    @Autowired
    private CreditBureauGateway creditBureauGateway;

    @Autowired
    private MessageGateway messageGateway;

    @GetMapping("/loan-quote")
    public String getLoanQuote(@RequestParam String ssn) {
        int creditScore = creditBureauGateway.getCreditScore(ssn);
        return "Loan quote for SSN " + ssn + " with credit score: " + creditScore;
    }

    @GetMapping("/loan-quote-async")
    public String getLoanQuoteAsync(@RequestParam String ssn) {
        messageGateway.sendAsyncCreditRequest(ssn);
        return "Credit request for SSN " + ssn + " submitted asynchronously";
    }

    @GetMapping("/loan-quote-topic")
    public String getLoanQuoteTopic(@RequestParam String ssn) {
        messageGateway.sendTopicCreditRequest(ssn);
        return "Credit request for SSN " + ssn + " sent to topic";
    }

    @GetMapping("/loan-quote-filtered")
    public String getLoanQuoteFiltered(@RequestParam String ssn) {
        messageGateway.sendFilteredCreditRequest(ssn);
        return "Filtered credit request for SSN " + ssn + " submitted";
    }

    @GetMapping("/loan-quote-idempotent")
    public String getLoanQuoteIdempotent(@RequestParam String ssn, @RequestParam String messageId) {
        messageGateway.sendIdempotentCreditRequest(ssn, messageId);
        return "Idempotent credit request for SSN " + ssn + " submitted";
    }

    @GetMapping("/loan-quote-async-service")
    public String getLoanQuoteAsyncService(@RequestParam String ssn) {
        messageGateway.sendAsyncServiceRequest(ssn);
        return "Async service credit request for SSN " + ssn + " submitted";
    }
}