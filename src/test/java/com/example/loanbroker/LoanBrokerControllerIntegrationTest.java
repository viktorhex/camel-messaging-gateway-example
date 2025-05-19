package com.example.loanbroker;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoanBrokerControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    private Connection connection;
    private Session session;
    private MessageConsumer topicConsumer1;
    private MessageConsumer topicConsumer2;

    @BeforeEach
    void setUp() throws JMSException {
        jmsTemplate.setReceiveTimeout(5000);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        topicConsumer1 = session.createConsumer((Destination) new ActiveMQTopic("creditTopic"));
        topicConsumer2 = session.createConsumer((Destination) new ActiveMQTopic("creditTopic"));
    }

    @AfterEach
    void tearDown() throws JMSException {
        if (topicConsumer1 != null) topicConsumer1.close();
        if (topicConsumer2 != null) topicConsumer2.close();
        if (session != null) session.close();
        if (connection != null) connection.close();
    }

    @Test
    void testSynchronousCreditRequest_validSsn_returnsScore() {
        jmsTemplate.convertAndSend(new ActiveMQQueue("creditRequest"), "123456789");
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("creditRequest"));
        assertTrue(Integer.parseInt(result) >= 0 && Integer.parseInt(result) < 1000);
    }

    @Test
    void testSynchronousCreditRequest_invalidSsn_returnsError() {
        jmsTemplate.convertAndSend(new ActiveMQQueue("creditRequest"), (Object) null);
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("creditRequest"));
        assertEquals("Error: Invalid input", result);
    }

    @Test
    void testAsynchronousCreditRequest_sendsToResultQueue() {
        jmsTemplate.convertAndSend(new ActiveMQQueue("asyncCreditRequest"), "123456789");
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("asyncResults"));
        assertTrue(Integer.parseInt(result) >= 0 && Integer.parseInt(result) < 1000);
    }

    @Test
    void testTopicCreditRequest_deliversToMultipleConsumers() throws JMSException {
        jmsTemplate.convertAndSend(new ActiveMQTopic("creditTopic"), "123456789");
        String result1 = (String) jmsTemplate.receiveAndConvert(new ActiveMQTopic("creditTopic"));
        String result2 = (String) jmsTemplate.receiveAndConvert(new ActiveMQTopic("creditTopic"));
        assertEquals("123456789", result1);
        assertEquals("123456789", result2);
    }

    @Test
    void testFilteredCreditRequest_validSsn_processes() {
        jmsTemplate.convertAndSend(new ActiveMQQueue("filteredCreditRequest"), "123456789");
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("filteredResults"));
        assertTrue(Integer.parseInt(result) >= 0 && Integer.parseInt(result) < 1000);
    }

    @Test
    void testFilteredCreditRequest_invalidSsn_skips() {
        jmsTemplate.convertAndSend(new ActiveMQQueue("filteredCreditRequest"), "123");
        Object result = jmsTemplate.receiveAndConvert(new ActiveMQQueue("filteredResults"));
        assertNull(result);
    }

    @Test
    void testIdempotentCreditRequest_deduplicatesMessages() {
        jmsTemplate.convertAndSend(new ActiveMQQueue("idempotentCreditRequest"), "123456789", message -> {
            message.setStringProperty("messageId", "msg1");
            return message;
        });
        jmsTemplate.convertAndSend(new ActiveMQQueue("idempotentCreditRequest"), "123456789", message -> {
            message.setStringProperty("messageId", "msg1");
            return message;
        });
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("idempotentResults"));
        assertTrue(Integer.parseInt(result) >= 0 && Integer.parseInt(result) < 1000);
        assertNull(jmsTemplate.receiveAndConvert(new ActiveMQQueue("idempotentResults")));
    }

    @Test
    void testAsyncServiceRequest_sendsToResultQueue() {
        jmsTemplate.convertAndSend(new ActiveMQQueue("asyncServiceRequest"), "123456789");
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("serviceResults"));
        assertTrue(Integer.parseInt(result) >= 0 && Integer.parseInt(result) < 1000);
    }

    @Test
    void testDurableSubscriber_receivesMessagesAfterReconnect() throws JMSException {
        Connection tempConnection = connectionFactory.createConnection();
        tempConnection.setClientID("testClient");
        tempConnection.start();
        Session tempSession = tempConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer durableConsumer = tempSession.createDurableSubscriber((Topic) new ActiveMQTopic("creditTopic"), "durableSub");
        durableConsumer.close();
        tempSession.close();
        tempConnection.close();

        jmsTemplate.convertAndSend(new ActiveMQTopic("creditTopic"), "123456789");

        tempConnection = connectionFactory.createConnection();
        tempConnection.setClientID("testClient");
        tempConnection.start();
        tempSession = tempConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        durableConsumer = tempSession.createDurableSubscriber((Topic) new ActiveMQTopic("creditTopic"), "durableSub");
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQTopic("creditTopic"));
        assertEquals("123456789", result);

        durableConsumer.close();
        tempSession.close();
        tempConnection.close();
    }

    @Test
    void testLoanQuote_validSsn_returnsQuote() {
        ResponseEntity<String> response = restTemplate.getForEntity("/loan-quote?ssn=123456789", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Loan quote for SSN 123456789 with credit score:"));
    }

    @Test
    void testLoanQuote_emptySsn_returnsQuote() {
        ResponseEntity<String> response = restTemplate.getForEntity("/loan-quote?ssn=", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Loan quote for SSN  with credit score: 0", response.getBody());
    }

    @Test
    void testLoanQuote_missingSsn_returnsBadRequest() {
        ResponseEntity<String> response = restTemplate.getForEntity("/loan-quote", String.class);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testAsyncCreditRequest_submitsSuccessfully() {
        ResponseEntity<String> response = restTemplate.getForEntity("/loan-quote-async?ssn=123456789", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Credit request for SSN 123456789 submitted asynchronously", response.getBody());
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("asyncResults"));
        assertTrue(Integer.parseInt(result) >= 0 && Integer.parseInt(result) < 1000);
    }

    @Test
    void testTopicCreditRequest_controllerDeliversToMultipleConsumers() throws JMSException {
        ResponseEntity<String> response = restTemplate.getForEntity("/loan-quote-topic?ssn=123456789", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Credit request for SSN 123456789 sent to topic", response.getBody());
        String result1 = (String) jmsTemplate.receiveAndConvert(new ActiveMQTopic("creditTopic"));
        String result2 = (String) jmsTemplate.receiveAndConvert(new ActiveMQTopic("creditTopic"));
        assertEquals("123456789", result1);
        assertEquals("123456789", result2);
    }

    @Test
    void testFilteredCreditRequest_controllerProcessesValidSsn() {
        ResponseEntity<String> response = restTemplate.getForEntity("/loan-quote-filtered?ssn=123456789", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Filtered credit request for SSN 123456789 submitted", response.getBody());
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("filteredResults"));
        assertTrue(Integer.parseInt(result) >= 0 && Integer.parseInt(result) < 1000);
    }

    @Test
    void testFilteredCreditRequest_controllerSkipsInvalidSsn() {
        ResponseEntity<String> response = restTemplate.getForEntity("/loan-quote-filtered?ssn=123", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Filtered credit request for SSN 123 submitted", response.getBody());
        Object result = jmsTemplate.receiveAndConvert(new ActiveMQQueue("filteredResults"));
        assertNull(result);
    }

    @Test
    void testIdempotentCreditRequest_controllerDeduplicates() {
        ResponseEntity<String> response1 = restTemplate.getForEntity("/loan-quote-idempotent?ssn=123456789&messageId=msg1", String.class);
        ResponseEntity<String> response2 = restTemplate.getForEntity("/loan-quote-idempotent?ssn=123456789&messageId=msg1", String.class);
        assertEquals(200, response1.getStatusCode().value());
        assertEquals("Idempotent credit request for SSN 123456789 submitted", response1.getBody());
        assertEquals(200, response2.getStatusCode().value());
        assertEquals("Idempotent credit request for SSN 123456789 submitted", response2.getBody());
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("idempotentResults"));
        assertTrue(Integer.parseInt(result) >= 0 && Integer.parseInt(result) < 1000);
        assertNull(jmsTemplate.receiveAndConvert(new ActiveMQQueue("idempotentResults")));
    }

    @Test
    void testAsyncServiceRequest_controllerSendsToResultQueue() {
        ResponseEntity<String> response = restTemplate.getForEntity("/loan-quote-async-service?ssn=123456789", String.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Async service credit request for SSN 123456789 submitted", response.getBody());
        String result = (String) jmsTemplate.receiveAndConvert(new ActiveMQQueue("serviceResults"));
        assertTrue(Integer.parseInt(result) >= 0 && Integer.parseInt(result) < 1000);
    }
}