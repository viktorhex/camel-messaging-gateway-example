package com.example.loanbroker;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class LoanBrokerControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Test
    void testGetLoanQuoteAsync_validSsn_sendsMessageToQueue() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String ssn = "123456789";
        String brokerUrl = "tcp://localhost:61616"; // Must match application.properties

        mockMvc.perform(get("/loan-quote/async").param("ssn", ssn))
                .andExpect(status().isOk())
                .andExpect(content().string("Credit request for SSN 123456789 submitted asynchronously"));

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createQueue("asyncCreditRequest"));
        TextMessage message = (TextMessage) consumer.receive(1000);
        assertEquals(ssn, message.getText(), "Message content should match the SSN");

        consumer.close();
        session.close();
        connection.close();
    }

    @Test
    void testGetLoanQuoteAsync_invalidSsn_returnsBadRequest() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String invalidSsn = "123";

        mockMvc.perform(get("/loan-quote/async").param("ssn", invalidSsn))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid SSN format"));
    }
}