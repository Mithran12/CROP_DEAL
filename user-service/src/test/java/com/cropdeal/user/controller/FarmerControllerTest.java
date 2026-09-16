package com.cropdeal.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cropdeal.user.dto.response.FarmerResponse;
import com.cropdeal.user.security.JwtAuthenticationFilter;
import com.cropdeal.user.security.JwtService;
import com.cropdeal.user.service.FarmerService;

@WebMvcTest(FarmerController.class)
@AutoConfigureMockMvc(addFilters = false)
class FarmerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FarmerService farmerService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createFarmer_shouldReturn201() throws Exception {

        FarmerResponse response = FarmerResponse.builder()
                .farmerId(1L)
                .authUserId(1L)
                .fullName("John Farmer")
                .email("john@gmail.com")
                .phone("9876543210")
                .build();

        when(farmerService.createFarmer(any()))
                .thenReturn(response);

        String request = """
                {
                    "authUserId": 1,
                    "fullName": "John Farmer",
                    "email": "john@gmail.com",
                    "phone": "9876543210",
                    "addressLine": "123 Farm Road",
                    "city": "Coimbatore",
                    "state": "Tamil Nadu",
                    "pincode": "641001",
                    "bankAccountNumber": "1234567890",
                    "ifscCode": "SBIN0001234",
                    "bankName": "State Bank of India"
                }
                """;

        mockMvc.perform(
                post("/api/users/farmers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.farmerId").value(1))
        .andExpect(jsonPath("$.fullName")
                .value("John Farmer"))
        .andExpect(jsonPath("$.email")
                .value("john@gmail.com"));
    }

    @Test
    void createFarmer_shouldReturn400ForInvalidRequest() throws Exception {

        String request = """
                {
                    "authUserId": 1,
                    "fullName": "",
                    "email": "wrong-email",
                    "phone": "",
                    "addressLine": "",
                    "city": "",
                    "state": "",
                    "pincode": "123",
                    "bankAccountNumber": "",
                    "ifscCode": ""
                }
                """;

        mockMvc.perform(
                post("/api/users/farmers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest());
    }
}