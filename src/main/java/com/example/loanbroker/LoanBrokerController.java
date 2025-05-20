package com.example.loanbroker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/loan-quote")
public class LoanBrokerController {
    private final CreditBureauGateway creditBureauGateway;

    @Autowired
    public LoanBrokerController(CreditBureauGateway creditBureauGateway) {
        this.creditBureauGateway = creditBureauGateway;
    }

    @GetMapping
    public String getLoanQuote(@RequestParam String ssn) {
        validateSsn(ssn);
        try {
            int creditScore = creditBureauGateway.getCreditScore(ssn);
            return String.format("Loan quote for SSN %s with credit score: %d", ssn, creditScore);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to retrieve credit score: " + e.getMessage());
        }
    }

    @GetMapping("/async")
    public String getLoanQuoteAsync(@RequestParam String ssn) {
        validateSsn(ssn);
        creditBureauGateway.requestAsyncCreditScore(ssn);
        return String.format("Credit request for SSN %s submitted asynchronously", ssn);
    }

    @GetMapping("/topic")
    public String getLoanQuoteTopic(@RequestParam String ssn) {
        validateSsn(ssn);
        creditBureauGateway.requestTopicCreditScore(ssn);
        return String.format("Credit request for SSN %s sent to topic", ssn);
    }

    @GetMapping("/filtered")
    public String getLoanQuoteFiltered(@RequestParam String ssn) {
        validateSsn(ssn);
        creditBureauGateway.requestFilteredCreditScore(ssn);
        return String.format("Filtered credit request for SSN %s submitted", ssn);
    }

    @GetMapping("/idempotent")
    public String getLoanQuoteIdempotent(@RequestParam String ssn, @RequestParam(required = false) String messageId) {
        validateSsn(ssn);
        validateMessageId(messageId);
        creditBureauGateway.requestIdempotentCreditScore(ssn, messageId);
        return String.format("Idempotent credit request for SSN %s submitted", ssn);
    }

    @GetMapping("/async-service")
    public String getLoanQuoteAsyncService(@RequestParam String ssn) {
        validateSsn(ssn);
        creditBureauGateway.requestAsyncServiceCreditScore(ssn);
        return String.format("Async service credit request for SSN %s submitted", ssn);
    }

    private void validateSsn(String ssn) {
        if (ssn == null || !ssn.matches("\\d{9}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid SSN format");
        }
    }

    private void validateMessageId(String messageId) {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message ID cannot be empty");
        }
    }
}