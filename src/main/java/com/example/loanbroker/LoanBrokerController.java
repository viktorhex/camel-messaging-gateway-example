package com.example.loanbroker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoanBrokerController {
    @Autowired
    private CreditBureauGateway creditBureauGateway;

    @GetMapping("/loan-quote")
    public String getLoanQuote(@RequestParam String ssn) {
        int creditScore = creditBureauGateway.getCreditScore(ssn);
        return "Loan quote for SSN " + ssn + " with credit score: " + creditScore;
    }
}
