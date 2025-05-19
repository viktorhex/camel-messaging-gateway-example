package com.example.camel_messaging_gateway_example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
public class MessageControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UppercaseGateway uppercaseGateway;

    @Test
    void shouldReturnUppercaseForValidInput() throws Exception {
        when(uppercaseGateway.sendToUppercaseQueue("hello")).thenReturn("HELLO");

        mockMvc.perform(get("/convert?text=hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("HELLO"));
    }

    @Test
    void shouldReturnEmptyForEmptyInput() throws Exception {
        when(uppercaseGateway.sendToUppercaseQueue("")).thenReturn("");

        mockMvc.perform(get("/convert?text="))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void shouldReturnBadRequestForMissingInput() throws Exception {
        mockMvc.perform(get("/convert"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }
}