package com.cropdeal.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cropdeal.order.dto.request.CreateOrderRequestDto;
import com.cropdeal.order.dto.request.UpdateOrderStatusRequestDto;
import com.cropdeal.order.dto.response.OrderResponseDto;
import com.cropdeal.order.enums.OrderStatus;
import com.cropdeal.order.enums.PaymentStatus;
import com.cropdeal.order.enums.PickupStatus;
import com.cropdeal.order.exception.GlobalExceptionHandler;
import com.cropdeal.order.exception.UnauthorizedResourceException;
import com.cropdeal.order.service.OrderService;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(orderController)
                        .setControllerAdvice(
                                new GlobalExceptionHandler())
                        .setCustomArgumentResolvers(
                                new AuthenticationPrincipalArgumentResolver())
                        .build();

        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrder_shouldReturnCreated() throws Exception {

        authenticate(
                301L,
                "DEALER",
                "dealer@example.com");

        CreateOrderRequestDto requestDto =
                new CreateOrderRequestDto();

        requestDto.setFarmerId(201L);
        requestDto.setCropId(101L);
        requestDto.setQuantity(
                new BigDecimal("100"));
        requestDto.setAgreedPrice(
                new BigDecimal("55.00"));

        OrderResponseDto responseDto =
                createOrderResponse(
                        1L,
                        OrderStatus.PAYMENT_PENDING);

        when(orderService.createOrder(
                any(CreateOrderRequestDto.class),
                eq(301L)))
                .thenReturn(responseDto);

        mockMvc.perform(
                post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.farmerId").value(201))
                .andExpect(jsonPath("$.dealerId").value(301))
                .andExpect(jsonPath("$.cropId").value(101))
                .andExpect(
                        jsonPath("$.status")
                                .value("PAYMENT_PENDING"));

        verify(orderService)
                .createOrder(
                        any(CreateOrderRequestDto.class),
                        eq(301L));
    }

    @Test
    void getOrderById_shouldReturnOk() throws Exception {

        authenticate(
                201L,
                "FARMER",
                "farmer@example.com");

        OrderResponseDto responseDto =
                createOrderResponse(
                        1L,
                        OrderStatus.PAYMENT_PENDING);

        when(orderService.getOrderById(
                1L,
                201L))
                .thenReturn(responseDto);

        mockMvc.perform(
                get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.status")
                                .value("PAYMENT_PENDING"));

        verify(orderService)
                .getOrderById(
                        1L,
                        201L);
    }

    @Test
    void getOrdersByFarmer_shouldReturnList()
            throws Exception {

        authenticate(
                201L,
                "FARMER",
                "farmer@example.com");

        OrderResponseDto order1 =
                createOrderResponse(
                        1L,
                        OrderStatus.PAID);

        OrderResponseDto order2 =
                createOrderResponse(
                        2L,
                        OrderStatus.READY_FOR_PICKUP);

        when(orderService.getOrdersByFarmer(
                201L,
                201L))
                .thenReturn(
                        List.of(order1, order2));

        mockMvc.perform(
                get("/api/orders/farmer/201"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$[0].id").value(1))
                .andExpect(
                        jsonPath("$[1].id").value(2));

        verify(orderService)
                .getOrdersByFarmer(
                        201L,
                        201L);
    }

    @Test
    void getOrdersByDealer_shouldReturnList()
            throws Exception {

        authenticate(
                301L,
                "DEALER",
                "dealer@example.com");

        OrderResponseDto order1 =
                createOrderResponse(
                        1L,
                        OrderStatus.PAID);

        OrderResponseDto order2 =
                createOrderResponse(
                        2L,
                        OrderStatus.COMPLETED);

        when(orderService.getOrdersByDealer(
                301L,
                301L))
                .thenReturn(
                        List.of(order1, order2));

        mockMvc.perform(
                get("/api/orders/dealer/301"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$[0].id").value(1))
                .andExpect(
                        jsonPath("$[1].id").value(2));

        verify(orderService)
                .getOrdersByDealer(
                        301L,
                        301L);
    }

    @Test
    void updateOrderStatus_shouldReturnOk()
            throws Exception {

        authenticate(
                301L,
                "DEALER",
                "dealer@example.com");

        UpdateOrderStatusRequestDto requestDto =
                new UpdateOrderStatusRequestDto();

        requestDto.setStatus(OrderStatus.PAID);
        requestDto.setReason(
                "Payment successful");

        OrderResponseDto responseDto =
                createOrderResponse(
                        1L,
                        OrderStatus.PAID);

        when(orderService.updateOrderStatus(
                eq(1L),
                any(UpdateOrderStatusRequestDto.class),
                eq(301L),
                eq("DEALER")))
                .thenReturn(responseDto);

        mockMvc.perform(
                patch("/api/orders/1/status")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        requestDto)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.status")
                                .value("PAID"));

        verify(orderService)
        .updateOrderStatus(
                eq(1L),
                any(UpdateOrderStatusRequestDto.class),
                eq(301L),
                eq("DEALER"));
    }

    @Test
    void cancelOrder_shouldReturnOk()
            throws Exception {

        authenticate(
                301L,
                "DEALER",
                "dealer@example.com");

        OrderResponseDto responseDto =
                createOrderResponse(
                        1L,
                        OrderStatus.CANCELLED);

        when(orderService.cancelOrder(
                1L,
                301L))
                .thenReturn(responseDto);

        mockMvc.perform(
                patch("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.status")
                                .value("CANCELLED"));

        verify(orderService)
                .cancelOrder(
                        1L,
                        301L);
    }

    @Test
    void completeOrder_shouldReturnOk()
            throws Exception {

        authenticate(
                301L,
                "DEALER",
                "dealer@example.com");

        OrderResponseDto responseDto =
                createOrderResponse(
                        1L,
                        OrderStatus.COMPLETED);

        responseDto.setCompletedAt(
                LocalDateTime.now());

        when(orderService.completeOrder(
                1L,
                301L))
                .thenReturn(responseDto);

        mockMvc.perform(
                patch("/api/orders/1/complete"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.status")
                                .value("COMPLETED"))
                .andExpect(
                        jsonPath("$.completedAt")
                                .exists());

        verify(orderService)
                .completeOrder(
                        1L,
                        301L);
    }

    @Test
    void createOrder_withInvalidData_shouldReturnBadRequest()
            throws Exception {

        authenticate(
                301L,
                "DEALER",
                "dealer@example.com");

        CreateOrderRequestDto requestDto =
                new CreateOrderRequestDto();

        requestDto.setFarmerId(201L);
        requestDto.setCropId(101L);
        requestDto.setQuantity(
                new BigDecimal("-100"));
        requestDto.setAgreedPrice(
                new BigDecimal("-55.00"));

        mockMvc.perform(
                post("/api/orders")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        requestDto)))
                .andExpect(
                        status().isBadRequest());
    }

    @Test
    void updateOrderStatus_withMissingStatus_shouldReturnBadRequest()
            throws Exception {

        authenticate(
                301L,
                "DEALER",
                "dealer@example.com");

        UpdateOrderStatusRequestDto requestDto =
                new UpdateOrderStatusRequestDto();

        requestDto.setReason(
                "Missing status");

        mockMvc.perform(
                patch("/api/orders/1/status")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        requestDto)))
                .andExpect(
                        status().isBadRequest());
    }

    @Test
    void getOrderById_shouldPassAuthenticatedUserId()
            throws Exception {

        authenticate(
                301L,
                "DEALER",
                "dealer@example.com");

        OrderResponseDto responseDto =
                createOrderResponse(
                        1L,
                        OrderStatus.PAID);

        when(orderService.getOrderById(
                1L,
                301L))
                .thenReturn(responseDto);

        mockMvc.perform(
                get("/api/orders/1"))
                .andExpect(
                        status().isOk());

        verify(orderService)
                .getOrderById(
                        1L,
                        301L);
    }

    @Test
    void getOrdersByFarmer_shouldPassAuthenticatedUserId()
            throws Exception {

        authenticate(
                201L,
                "FARMER",
                "farmer@example.com");

        when(orderService.getOrdersByFarmer(
                201L,
                201L))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/orders/farmer/201"))
                .andExpect(
                        status().isOk());

        verify(orderService)
                .getOrdersByFarmer(
                        201L,
                        201L);
    }

    @Test
    void getOrdersByDealer_shouldPassAuthenticatedUserId()
            throws Exception {

        authenticate(
                301L,
                "DEALER",
                "dealer@example.com");

        when(orderService.getOrdersByDealer(
                301L,
                301L))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/orders/dealer/301"))
                .andExpect(
                        status().isOk());

        verify(orderService)
                .getOrdersByDealer(
                        301L,
                        301L);
    }

    @Test
    void getOrderById_whenServiceDeniesAccess_shouldReturnForbidden()
            throws Exception {

        authenticate(
                999L,
                "DEALER",
                "otherdealer@example.com");

        when(orderService.getOrderById(
                1L,
                999L))
                .thenThrow(
                        new UnauthorizedResourceException(
                                "You are not authorized to access this order"));

        mockMvc.perform(
                get("/api/orders/1"))
                .andExpect(
                        status().isForbidden());

        verify(orderService)
                .getOrderById(
                        1L,
                        999L);
    }

    @Test
    void getOrdersByFarmer_whenServiceDeniesAccess_shouldReturnForbidden()
            throws Exception {

        authenticate(
                202L,
                "FARMER",
                "otherfarmer@example.com");

        when(orderService.getOrdersByFarmer(
                201L,
                202L))
                .thenThrow(
                        new UnauthorizedResourceException(
                                "You are not authorized to access these farmer orders"));

        mockMvc.perform(
                get("/api/orders/farmer/201"))
                .andExpect(
                        status().isForbidden());

        verify(orderService)
                .getOrdersByFarmer(
                        201L,
                        202L);
    }

    @Test
    void getOrdersByDealer_whenServiceDeniesAccess_shouldReturnForbidden()
            throws Exception {

        authenticate(
                302L,
                "DEALER",
                "otherdealer@example.com");

        when(orderService.getOrdersByDealer(
                301L,
                302L))
                .thenThrow(
                        new UnauthorizedResourceException(
                                "You are not authorized to access these dealer orders"));

        mockMvc.perform(
                get("/api/orders/dealer/301"))
                .andExpect(
                        status().isForbidden());

        verify(orderService)
                .getOrdersByDealer(
                        301L,
                        302L);
    }

    private void authenticate(
            Long userId,
            String role,
            String email) {

        Jwt jwt =
                Jwt.withTokenValue("test-token")
                        .header("alg", "HS256")
                        .subject(email)
                        .claim("userId", userId)
                        .claim("role", role)
                        .build();

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(jwt);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    private OrderResponseDto createOrderResponse(
            Long id,
            OrderStatus status) {

        OrderResponseDto response =
                new OrderResponseDto();

        response.setId(id);
        response.setOrderNumber("ORD-" + id);

        response.setFarmerId(201L);
        response.setDealerId(301L);
        response.setCropId(101L);

        response.setQuantity(
                new BigDecimal("100"));

        response.setAgreedPrice(
                new BigDecimal("55.00"));

        response.setTotalAmount(
                new BigDecimal("5500.00"));

        response.setStatus(status);

        response.setPaymentStatus(
                status == OrderStatus.PAID
                        ? PaymentStatus.PAID
                        : PaymentStatus.PENDING);

        response.setPickupStatus(
                status == OrderStatus.COMPLETED
                        ? PickupStatus.PICKED_UP
                        : PickupStatus.PENDING);

        response.setCreatedAt(
                LocalDateTime.now());

        response.setUpdatedAt(
                LocalDateTime.now());

        return response;
    }
}