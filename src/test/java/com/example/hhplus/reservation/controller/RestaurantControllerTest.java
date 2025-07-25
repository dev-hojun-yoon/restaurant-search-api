package com.example.hhplus.reservation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRestaurants() throws Exception {
        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isOk());
    }
}
