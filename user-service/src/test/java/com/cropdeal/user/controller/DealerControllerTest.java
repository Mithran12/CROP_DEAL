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

import com.cropdeal.user.dto.response.DealerResponse;
import com.cropdeal.user.security.JwtAuthenticationFilter;
import com.cropdeal.user.security.JwtService;
import com.cropdeal.user.service.DealerService;

@WebMvcTest(DealerController.class)
@AutoConfigureMockMvc(addFilters = false)
class DealerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DealerService dealerService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createDealer_shouldReturn201() throws Exception {

        DealerResponse response = DealerResponse.builder()
                .dealerId(1L)
                .authUserId(2L)
                .businessName("ABC Agro Traders")
                .contactPersonName("John Dealer")
                .email("dealer@gmail.com")
                .phone("9876543210")
                .build();

        when(dealerService.createDealer(any()))
                .thenReturn(response);

        String request = """
                {
                    "authUserId": 2,
                    "businessName": "ABC Agro Traders",
                    "contactPersonName": "John Dealer",
                    "email": "dealer@gmail.com",
                    "phone": "9876543210",
                    "businessType": "Agricultural Wholesale",
                    "addressLine": "45 Market Road",
                    "city": "Coimbatore",
                    "state": "Tamil Nadu",
                    "pincode": "641002",
                    "bankAccountNumber": "9876543210",
                    "ifscCode": "HDFC0001234",
                    "bankName": "HDFC Bank"
                }
                """;

        mockMvc.perform(
                post("/api/users/dealers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.dealerId").value(1))
        .andExpect(jsonPath("$.businessName")
                .value("ABC Agro Traders"))
        .andExpect(jsonPath("$.email")
                .value("dealer@gmail.com"));
    }

    @Test
    void createDealer_shouldReturn400ForInvalidRequest() throws Exception {

        String request = """
                {
                    "authUserId": 2,
                    "businessName": "",
                    "contactPersonName": "",
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
                post("/api/users/dealers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest());
    }
}