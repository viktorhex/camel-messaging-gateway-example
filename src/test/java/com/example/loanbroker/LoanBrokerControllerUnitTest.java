package com.example.loanbroker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanBrokerController.class)
public class LoanBrokerControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreditBureauGateway creditBureauGateway;

    @Test
    void shouldReturnLoanQuoteForValidSsn() throws Exception {
        when(creditBureauGateway.getCreditScore("123456789")).thenReturn(750);

        mockMvc.perform(get("/loan-quote?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan quote for SSN 123456789 with credit score: 750"));
    }

    @Test
    void shouldReturnEmptyForEmptySsn() throws Exception {
        when(creditBureauGateway.getCreditScore("")).thenReturn(0);

        mockMvc.perform(get("/loan-quote?ssn="))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan quote for SSN  with credit score: 0"));
    }

    @Test
    void shouldReturnBadRequestForMissingSsn() throws Exception {
        mockMvc.perform(get("/loan-quote"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }
}