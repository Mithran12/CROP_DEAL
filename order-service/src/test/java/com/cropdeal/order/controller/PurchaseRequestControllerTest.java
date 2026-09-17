package com.cropdeal.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.cropdeal.order.dto.request.CreatePurchaseRequestDto;
import com.cropdeal.order.dto.response.PurchaseRequestResponseDto;
import com.cropdeal.order.enums.PurchaseRequestStatus;
import com.cropdeal.order.exception.BusinessException;
import com.cropdeal.order.exception.GlobalExceptionHandler;
import com.cropdeal.order.exception.ResourceNotFoundException;
import com.cropdeal.order.service.PurchaseRequestService;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PurchaseRequestControllerTest {

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private PurchaseRequestService purchaseRequestService;

	@InjectMocks
	private PurchaseRequestController purchaseRequestController;

	@BeforeEach
	void setUp() {

		mockMvc = MockMvcBuilders.standaloneSetup(purchaseRequestController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();

		SecurityContextHolder.clearContext();
	}

	// =========================================================
	// 1. CREATE PURCHASE REQUEST
	// =========================================================

	@Test
	void createPurchaseRequest_shouldReturnCreated() throws Exception {

		authenticateAsDealer();

		CreatePurchaseRequestDto requestDto = new CreatePurchaseRequestDto();

		requestDto.setCropId(101L);
		requestDto.setRequestedQuantity(new BigDecimal("100"));
		requestDto.setOfferedPrice(new BigDecimal("50.00"));
		requestDto.setMessage("Need good quality crop");

		PurchaseRequestResponseDto responseDto = createResponseDto(1L, PurchaseRequestStatus.PENDING);

		when(purchaseRequestService.createPurchaseRequest(any(CreatePurchaseRequestDto.class), eq(301L)))
				.thenReturn(responseDto);

		mockMvc.perform(post("/api/purchase-requests").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.dealerId").value(301))
				.andExpect(jsonPath("$.status").value("PENDING"));

		verify(purchaseRequestService).createPurchaseRequest(any(CreatePurchaseRequestDto.class), eq(301L));
	}

	// =========================================================
	// 2. GET PURCHASE REQUEST BY ID
	// =========================================================

	@Test
	void getPurchaseRequestById_shouldReturnOk() throws Exception {

		authenticateAsDealer();

		PurchaseRequestResponseDto responseDto = createResponseDto(1L, PurchaseRequestStatus.PENDING);

		when(purchaseRequestService.getPurchaseRequestById(1L, 301L)).thenReturn(responseDto);

		mockMvc.perform(get("/api/purchase-requests/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.status").value("PENDING"));

		verify(purchaseRequestService).getPurchaseRequestById(1L, 301L);
	}

	// =========================================================
	// 3. GET PURCHASE REQUESTS BY FARMER
	// =========================================================

	@Test
	void getPurchaseRequestsByFarmer_shouldReturnList() throws Exception {

		authenticateAsFarmer();

		PurchaseRequestResponseDto response1 = createResponseDto(1L, PurchaseRequestStatus.PENDING);

		PurchaseRequestResponseDto response2 = createResponseDto(2L, PurchaseRequestStatus.ACCEPTED);

		when(purchaseRequestService.getPurchaseRequestsByFarmer(201L, 201L)).thenReturn(List.of(response1, response2));

		mockMvc.perform(get("/api/purchase-requests/farmer/201")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].id").value(2));

		verify(purchaseRequestService).getPurchaseRequestsByFarmer(201L, 201L);
	}

	// =========================================================
	// 4. GET PURCHASE REQUESTS BY DEALER
	// =========================================================

	@Test
	void getPurchaseRequestsByDealer_shouldReturnList() throws Exception {

		authenticateAsDealer();

		PurchaseRequestResponseDto response1 = createResponseDto(1L, PurchaseRequestStatus.PENDING);

		PurchaseRequestResponseDto response2 = createResponseDto(2L, PurchaseRequestStatus.REJECTED);

		when(purchaseRequestService.getPurchaseRequestsByDealer(301L, 301L)).thenReturn(List.of(response1, response2));

		mockMvc.perform(get("/api/purchase-requests/dealer/301")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].id").value(2));

		verify(purchaseRequestService).getPurchaseRequestsByDealer(301L, 301L);
	}

	// =========================================================
	// 5. ACCEPT PURCHASE REQUEST
	// =========================================================

	@Test
	void acceptPurchaseRequest_shouldReturnOk() throws Exception {

		authenticateAsFarmer();

		PurchaseRequestResponseDto responseDto = createResponseDto(1L, PurchaseRequestStatus.ACCEPTED);

		when(purchaseRequestService.acceptPurchaseRequest(1L, 201L)).thenReturn(responseDto);

		mockMvc.perform(patch("/api/purchase-requests/1/accept")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACCEPTED"));

		verify(purchaseRequestService).acceptPurchaseRequest(1L, 201L);
	}

	// =========================================================
	// 6. REJECT PURCHASE REQUEST
	// =========================================================

	@Test
	void rejectPurchaseRequest_shouldReturnOk() throws Exception {

		authenticateAsFarmer();

		PurchaseRequestResponseDto responseDto = createResponseDto(1L, PurchaseRequestStatus.REJECTED);

		when(purchaseRequestService.rejectPurchaseRequest(1L, 201L)).thenReturn(responseDto);

		mockMvc.perform(patch("/api/purchase-requests/1/reject")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"));

		verify(purchaseRequestService).rejectPurchaseRequest(1L, 201L);
	}

	// =========================================================
	// 7. GET PURCHASE REQUEST BY ID - NOT FOUND
	// =========================================================

	@Test
	void getPurchaseRequestById_whenNotFound_shouldReturnNotFound() throws Exception {

		authenticateAsDealer();

		when(purchaseRequestService.getPurchaseRequestById(999L, 301L))
				.thenThrow(new ResourceNotFoundException("Purchase request not found with id: 999"));

		mockMvc.perform(get("/api/purchase-requests/999")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Purchase request not found with id: 999"))
				.andExpect(jsonPath("$.path").value("/api/purchase-requests/999"));

		verify(purchaseRequestService).getPurchaseRequestById(999L, 301L);
	}

	// =========================================================
	// 8. CREATE PURCHASE REQUEST - INVALID DATA
	// =========================================================

	@Test
	void createPurchaseRequest_withInvalidData_shouldReturnBadRequest() throws Exception {

		authenticateAsDealer();

		CreatePurchaseRequestDto requestDto = new CreatePurchaseRequestDto();

		requestDto.setCropId(101L);
		requestDto.setRequestedQuantity(new BigDecimal("-10"));
		requestDto.setOfferedPrice(new BigDecimal("-50.00"));

		mockMvc.perform(post("/api/purchase-requests").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isBadRequest());
	}

	// =========================================================
	// 9. CANCEL PURCHASE REQUEST
	// =========================================================

	@Test
	void cancelPurchaseRequest_shouldReturnOk() throws Exception {

		authenticateAsDealer();

		PurchaseRequestResponseDto responseDto = createResponseDto(1L, PurchaseRequestStatus.CANCELLED);

		when(purchaseRequestService.cancelPurchaseRequest(1L, 301L)).thenReturn(responseDto);

		mockMvc.perform(patch("/api/purchase-requests/1/cancel")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));

		verify(purchaseRequestService).cancelPurchaseRequest(1L, 301L);
	}

	// =========================================================
	// 10. ACCEPT - BUSINESS RULE FAILURE
	// =========================================================

	@Test
	void acceptPurchaseRequest_whenBusinessRuleFails_shouldReturnBadRequest() throws Exception {

		authenticateAsFarmer();

		when(purchaseRequestService.acceptPurchaseRequest(1L, 201L))
				.thenThrow(new BusinessException("Only pending purchase requests can be accepted"));

		mockMvc.perform(patch("/api/purchase-requests/1/accept")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Only pending purchase requests can be accepted"))
				.andExpect(jsonPath("$.path").value("/api/purchase-requests/1/accept"));

		verify(purchaseRequestService).acceptPurchaseRequest(1L, 201L);
	}

	// =========================================================
	// AUTHENTICATION HELPERS
	// =========================================================

	private void authenticateAsDealer() {

		Jwt jwt = Jwt.withTokenValue("test-dealer-token").header("alg", "HS256").subject("dealer@example.com")
				.claim("userId", 301L).claim("role", "DEALER").build();

		JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void authenticateAsFarmer() {

		Jwt jwt = Jwt.withTokenValue("test-farmer-token").header("alg", "HS256").subject("farmer@example.com")
				.claim("userId", 201L).claim("role", "FARMER").build();

		JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	// =========================================================
	// RESPONSE DTO HELPER
	// =========================================================

	private PurchaseRequestResponseDto createResponseDto(Long id, PurchaseRequestStatus status) {

		PurchaseRequestResponseDto response = new PurchaseRequestResponseDto();

		response.setId(id);
		response.setRequestNumber("REQ-" + id);
		response.setCropId(101L);
		response.setFarmerId(201L);
		response.setDealerId(301L);
		response.setRequestedQuantity(new BigDecimal("100"));
		response.setOfferedPrice(new BigDecimal("50.00"));
		response.setMessage("Test request");
		response.setStatus(status);
		response.setCreatedAt(LocalDateTime.now());
		response.setUpdatedAt(LocalDateTime.now());

		return response;
	}
}