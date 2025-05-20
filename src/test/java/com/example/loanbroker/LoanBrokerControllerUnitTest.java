package com.example.loanbroker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {LoanBrokerController.class, GlobalExceptionHandler.class})
public class LoanBrokerControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreditBureauGateway creditBureauGateway;

    @Test
    void shouldReturnLoanQuoteWhenValidSsn() throws Exception {
        when(creditBureauGateway.getCreditScore("123456789")).thenReturn(750);

        mockMvc.perform(get("/loan-quote?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Loan quote for SSN 123456789 with credit score: 750")));

        verify(creditBureauGateway).getCreditScore("123456789");
    }

    @Test
    void shouldReturnBadRequestWhenSsnMissing() throws Exception {
        mockMvc.perform(get("/loan-quote"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12345678", "1234567890", "123-45-6789", "abc"})
    void shouldReturnBadRequestWhenSsnFormatInvalid(String ssn) throws Exception {
        mockMvc.perform(get("/loan-quote?ssn=" + ssn))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid SSN format")));
    }

    @Test
    void shouldReturnBadRequestWhenCreditScoreInvalid() throws Exception {
        when(creditBureauGateway.getCreditScore("123456789")).thenThrow(new RuntimeException("Invalid credit score response: invalid"));

        mockMvc.perform(get("/loan-quote?ssn=123456789"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Failed to retrieve credit score")));

        verify(creditBureauGateway).getCreditScore("123456789");
    }

    @Test
    void shouldSubmitAsyncCreditRequestWhenValidSsn() throws Exception {
        mockMvc.perform(get("/loan-quote/async?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Credit request for SSN 123456789 submitted asynchronously")));

        verify(creditBureauGateway).requestAsyncCreditScore("123456789");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12345678", "1234567890", "123-45-6789", "abc"})
    void shouldReturnBadRequestWhenSsnInvalidForAsync(String ssn) throws Exception {
        mockMvc.perform(get("/loan-quote/async?ssn=" + ssn))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid SSN format")));
    }

    @Test
    void shouldSubmitTopicCreditRequestWhenValidSsn() throws Exception {
        mockMvc.perform(get("/loan-quote/topic?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Credit request for SSN 123456789 sent to topic")));

        verify(creditBureauGateway).requestTopicCreditScore("123456789");
    }

    @Test
    void shouldSubmitFilteredCreditRequestWhenValidSsn() throws Exception {
        mockMvc.perform(get("/loan-quote/filtered?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Filtered credit request for SSN 123456789 submitted")));

        verify(creditBureauGateway).requestFilteredCreditScore("123456789");
    }

    @Test
    void shouldSubmitIdempotentCreditRequestWhenValidSsnAndMessageId() throws Exception {
        mockMvc.perform(get("/loan-quote/idempotent?ssn=123456789&messageId=msg1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Idempotent credit request for SSN 123456789 submitted")));

        verify(creditBureauGateway).requestIdempotentCreditScore("123456789", "msg1");
    }

    @Test
    void shouldReturnBadRequestWhenMessageIdMissingForIdempotent() throws Exception {
        mockMvc.perform(get("/loan-quote/idempotent?ssn=123456789"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Message ID cannot be empty")));
    }

    @Test
    void shouldReturnBadRequestWhenMessageIdEmptyForIdempotent() throws Exception {
        mockMvc.perform(get("/loan-quote/idempotent?ssn=123456789&messageId="))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Message ID cannot be empty")));
    }

    @Test
    void shouldSubmitAsyncServiceRequestWhenValidSsn() throws Exception {
        mockMvc.perform(get("/loan-quote/async-service?ssn=123456789"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Async service credit request for SSN 123456789 submitted")));

        verify(creditBureauGateway).requestAsyncServiceCreditScore("123456789");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "12345678", "1234567890", "123-45-6789", "abc"})
    void shouldReturnBadRequestWhenSsnInvalidForAsyncService(String ssn) throws Exception {
        mockMvc.perform(get("/loan-quote/async-service?ssn=" + ssn))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid SSN format")));
    }
}