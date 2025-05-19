package com.example.loanbroker;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanBrokerController.class)
@ComponentScan(basePackages = "com.example.loanbroker")
public class LoanBrokerControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageGateway messageGateway;

    @Test
    void shouldReturnLoanQuoteForValidSsn() throws Exception {
        when(messageGateway.sendCreditRequest("123456789")).thenReturn("750");

        mockMvc.perform(get("/loan-quote?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan quote for SSN 123456789 with credit score: 750"));

        verify(messageGateway).sendCreditRequest("123456789");
    }

    @Test
    void shouldReturnEmptyForEmptySsn() throws Exception {
        when(messageGateway.sendCreditRequest("")).thenReturn("0");

        mockMvc.perform(get("/loan-quote?ssn="))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan quote for SSN  with credit score: 0"));

        verify(messageGateway).sendCreditRequest("");
    }

    @Test
    void shouldReturnBadRequestForMissingSsn() throws Exception {
        mockMvc.perform(get("/loan-quote"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    // @Test
    // void shouldHandleInvalidCreditResponse() throws Exception {
    //     when(messageGateway.sendCreditRequest("123456789")).thenReturn("Error: Invalid input");

    //     try {
    //         MvcResult result = mockMvc.perform(get("/loan-quote?ssn=123456789")).andReturn();
    //         assert result.getResponse().getStatus() == 500 : "Expected HTTP 500 status";
    //     } catch (ServletException e) {
    //         // Expected exception due to RuntimeException
    //     }

    //     verify(messageGateway).sendCreditRequest("123456789");
    // }

    @Test
    void shouldSubmitAsyncCreditRequest() throws Exception {
        mockMvc.perform(get("/loan-quote-async?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string("Credit request for SSN 123456789 submitted asynchronously"));

        verify(messageGateway).sendAsyncCreditRequest("123456789");
    }

    @Test
    void shouldSubmitAsyncCreditRequestForEmptySsn() throws Exception {
        mockMvc.perform(get("/loan-quote-async?ssn="))
                .andExpect(status().isOk())
                .andExpect(content().string("Credit request for SSN  submitted asynchronously"));

        verify(messageGateway).sendAsyncCreditRequest("");
    }

    @Test
    void shouldReturnBadRequestForMissingSsnInAsync() throws Exception {
        mockMvc.perform(get("/loan-quote-async"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldSubmitTopicCreditRequest() throws Exception {
        mockMvc.perform(get("/loan-quote-topic?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string("Credit request for SSN 123456789 sent to topic"));

        verify(messageGateway).sendTopicCreditRequest("123456789");
    }

    @Test
    void shouldSubmitFilteredCreditRequest() throws Exception {
        mockMvc.perform(get("/loan-quote-filtered?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string("Filtered credit request for SSN 123456789 submitted"));

        verify(messageGateway).sendFilteredCreditRequest("123456789");
    }

    @Test
    void shouldSubmitIdempotentCreditRequest() throws Exception {
        mockMvc.perform(get("/loan-quote-idempotent?ssn=123456789&messageId=msg1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Idempotent credit request for SSN 123456789 submitted"));

        verify(messageGateway).sendIdempotentCreditRequest("123456789", "msg1");
    }

    @Test
    void shouldReturnBadRequestForMissingMessageIdInIdempotent() throws Exception {
        mockMvc.perform(get("/loan-quote-idempotent?ssn=123456789"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void shouldSubmitAsyncServiceRequest() throws Exception {
        mockMvc.perform(get("/loan-quote-async-service?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string("Async service credit request for SSN 123456789 submitted"));

        verify(messageGateway).sendAsyncServiceRequest("123456789");
    }

    @Test
    void shouldSubmitAsyncServiceRequestForEmptySsn() throws Exception {
        mockMvc.perform(get("/loan-quote-async-service?ssn="))
                .andExpect(status().isOk())
                .andExpect(content().string("Async service credit request for SSN  submitted"));

        verify(messageGateway).sendAsyncServiceRequest("");
    }

    @Test
    void shouldReturnBadRequestForMissingSsnInAsyncService() throws Exception {
        mockMvc.perform(get("/loan-quote-async-service"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }
}