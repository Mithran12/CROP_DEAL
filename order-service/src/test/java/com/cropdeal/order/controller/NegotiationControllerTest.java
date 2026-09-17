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

import com.cropdeal.order.dto.request.CounterOfferRequestDto;
import com.cropdeal.order.dto.request.CreateOfferRequestDto;
import com.cropdeal.order.dto.response.NegotiationResponseDto;
import com.cropdeal.order.dto.response.OfferResponseDto;
import com.cropdeal.order.enums.NegotiationStatus;
import com.cropdeal.order.enums.OfferStatus;
import com.cropdeal.order.service.NegotiationService;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class NegotiationControllerTest {

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private NegotiationService negotiationService;

	@InjectMocks
	private NegotiationController negotiationController;

	@BeforeEach
	void setUp() {

		mockMvc = MockMvcBuilders.standaloneSetup(negotiationController)
				.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();

		SecurityContextHolder.clearContext();
	}

	@Test
	void createNegotiation_shouldReturnCreated() throws Exception {

		authenticate(301L, "DEALER", "dealer@example.com");

		NegotiationResponseDto responseDto = createNegotiationResponse(1L, NegotiationStatus.ACTIVE);

		when(negotiationService.createNegotiation(10L, 301L)).thenReturn(responseDto);

		mockMvc.perform(post("/api/negotiations/10")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.purchaseRequestId").value(10))
				.andExpect(jsonPath("$.status").value("ACTIVE"));

		verify(negotiationService).createNegotiation(10L, 301L);
	}

	@Test
	void getNegotiationById_shouldReturnOk() throws Exception {

		authenticate(201L, "FARMER", "farmer@example.com");

		NegotiationResponseDto responseDto = createNegotiationResponse(1L, NegotiationStatus.ACTIVE);

		when(negotiationService.getNegotiationById(1L, 201L)).thenReturn(responseDto);

		mockMvc.perform(get("/api/negotiations/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.status").value("ACTIVE"));

		verify(negotiationService).getNegotiationById(1L, 201L);
	}

	@Test
	void getOffers_shouldReturnList() throws Exception {

		authenticate(301L, "DEALER", "dealer@example.com");

		OfferResponseDto offer1 = createOfferResponse(1L, OfferStatus.PENDING);

		OfferResponseDto offer2 = createOfferResponse(2L, OfferStatus.COUNTERED);

		when(negotiationService.getOffers(1L, 301L)).thenReturn(List.of(offer1, offer2));

		mockMvc.perform(get("/api/negotiations/1/offers")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].id").value(2));

		verify(negotiationService).getOffers(1L, 301L);
	}

	@Test
	void createOffer_shouldReturnCreated() throws Exception {

		authenticate(301L, "DEALER", "dealer@example.com");

		CreateOfferRequestDto requestDto = new CreateOfferRequestDto();

		requestDto.setPrice(new BigDecimal("55.00"));

		requestDto.setQuantity(new BigDecimal("100"));

		requestDto.setMessage("My offer");

		OfferResponseDto responseDto = createOfferResponse(1L, OfferStatus.PENDING);

		when(negotiationService.createOffer(any(Long.class), any(CreateOfferRequestDto.class), eq(301L), eq("DEALER")))
				.thenReturn(responseDto);

		mockMvc.perform(post("/api/negotiations/1/offer").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.negotiationId").value(1))
				.andExpect(jsonPath("$.offerStatus").value("PENDING"));

		verify(negotiationService).createOffer(any(Long.class), any(CreateOfferRequestDto.class), eq(301L),
				eq("DEALER"));
	}

	@Test
	void createCounterOffer_shouldReturnCreated() throws Exception {

		authenticate(201L, "FARMER", "farmer@example.com");

		CounterOfferRequestDto requestDto = new CounterOfferRequestDto();

		requestDto.setPrice(new BigDecimal("60.00"));

		requestDto.setQuantity(new BigDecimal("100"));

		requestDto.setMessage("Counter offer");

		OfferResponseDto responseDto = createOfferResponse(2L, OfferStatus.PENDING);

		when(negotiationService.createCounterOffer(any(Long.class), any(CounterOfferRequestDto.class), eq(201L),
				eq("FARMER"))).thenReturn(responseDto);

		mockMvc.perform(post("/api/negotiations/1/counter-offer").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(2)).andExpect(jsonPath("$.negotiationId").value(1))
				.andExpect(jsonPath("$.offerStatus").value("PENDING"));

		verify(negotiationService).createCounterOffer(any(Long.class), any(CounterOfferRequestDto.class), eq(201L),
				eq("FARMER"));
	}

	@Test
	void acceptOffer_shouldReturnOk() throws Exception {

		authenticate(201L, "FARMER", "farmer@example.com");

		NegotiationResponseDto responseDto = createNegotiationResponse(1L, NegotiationStatus.ACCEPTED);

		responseDto.setFinalPrice(new BigDecimal("55.00"));

		when(negotiationService.acceptOffer(1L, 5L, 201L)).thenReturn(responseDto);

		mockMvc.perform(patch("/api/negotiations/1/accept/5")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACCEPTED")).andExpect(jsonPath("$.finalPrice").value(55.00));

		verify(negotiationService).acceptOffer(1L, 5L, 201L);
	}

	@Test
	void rejectOffer_shouldReturnOk() throws Exception {

		authenticate(301L, "DEALER", "dealer@example.com");

		NegotiationResponseDto responseDto = createNegotiationResponse(1L, NegotiationStatus.ACTIVE);

		when(negotiationService.rejectOffer(1L, 5L, 301L)).thenReturn(responseDto);

		mockMvc.perform(patch("/api/negotiations/1/reject/5")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"));

		verify(negotiationService).rejectOffer(1L, 5L, 301L);
	}

	@Test
	void createOffer_withInvalidData_shouldReturnBadRequest() throws Exception {

		authenticate(301L, "DEALER", "dealer@example.com");

		CreateOfferRequestDto requestDto = new CreateOfferRequestDto();

		requestDto.setPrice(new BigDecimal("-10.00"));

		requestDto.setQuantity(new BigDecimal("-100"));

		requestDto.setMessage("Invalid offer");

		mockMvc.perform(post("/api/negotiations/1/offer").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isBadRequest());
	}

	@Test
	void createCounterOffer_withInvalidData_shouldReturnBadRequest() throws Exception {

		authenticate(201L, "FARMER", "farmer@example.com");

		CounterOfferRequestDto requestDto = new CounterOfferRequestDto();

		requestDto.setPrice(new BigDecimal("-10.00"));

		requestDto.setQuantity(new BigDecimal("-100"));

		requestDto.setMessage("Invalid counter offer");

		mockMvc.perform(post("/api/negotiations/1/counter-offer").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isBadRequest());
	}

	private void authenticate(Long userId, String role, String email) {

		Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "HS256").subject(email).claim("userId", userId)
				.claim("role", role).build();

		JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private NegotiationResponseDto createNegotiationResponse(Long id, NegotiationStatus status) {

		NegotiationResponseDto response = new NegotiationResponseDto();

		response.setId(id);

		response.setNegotiationNumber("NEG-" + id);

		response.setPurchaseRequestId(10L);

		response.setCropId(101L);

		response.setFarmerId(201L);

		response.setDealerId(301L);

		response.setStatus(status);

		response.setCreatedAt(LocalDateTime.now());

		response.setUpdatedAt(LocalDateTime.now());

		return response;
	}

	private OfferResponseDto createOfferResponse(Long id, OfferStatus status) {

		OfferResponseDto response = new OfferResponseDto();

		response.setId(id);

		response.setNegotiationId(1L);

		response.setOfferedByUserId(301L);

		response.setOfferedByRole("DEALER");

		response.setPrice(new BigDecimal("55.00"));

		response.setQuantity(new BigDecimal("100"));

		response.setMessage("Test offer");

		response.setOfferStatus(status);

		response.setCreatedAt(LocalDateTime.now());

		return response;
	}
}