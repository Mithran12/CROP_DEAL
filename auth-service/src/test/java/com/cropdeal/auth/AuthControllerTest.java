package com.cropdeal.auth;

import com.cropdeal.auth.controller.AuthController;
import com.cropdeal.auth.dto.FarmerRegisterRequest;
import com.cropdeal.auth.entity.Role;
import com.cropdeal.auth.entity.User;
import com.cropdeal.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthControllerTest {

    @Test
    void farmerRegistrationReturnsCreated() {
        AuthService service = Mockito.mock(AuthService.class);
        AuthController controller = new AuthController(service);

        User user = new User();
        user.setId(1L);
        user.setName("Test Farmer");
        user.setEmail("farmer@example.com");
        user.setRole(Role.FARMER);
        user.setActive(true);
        Mockito.when(service.registerFarmer(Mockito.any())).thenReturn(user);

        FarmerRegisterRequest request = new FarmerRegisterRequest();
        request.setName("Test Farmer");
        request.setEmail("farmer@example.com");
        request.setPassword("secret123");

        var response = controller.registerFarmer(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("FARMER", response.getBody().getRole());
    }
}
