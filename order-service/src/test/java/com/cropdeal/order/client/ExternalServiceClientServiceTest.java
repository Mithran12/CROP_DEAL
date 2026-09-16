package com.cropdeal.order.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.cropdeal.order.exception.ExternalServiceException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalServiceClientServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CropServiceClient cropServiceClient;

    @InjectMocks
    private ExternalServiceClientService externalServiceClientService;

    @Test
    void shouldGetUserSuccessfully() {

        Object expectedUser = new Object();

        when(userServiceClient.getUserById(1L))
                .thenReturn(expectedUser);

        Object result =
                externalServiceClientService.getUserById(1L);

        assertEquals(expectedUser, result);
    }

    @Test
    void shouldGetCropSuccessfully() {

        Object expectedCrop = new Object();

        when(cropServiceClient.getCropById(1L))
                .thenReturn(expectedCrop);

        Object result =
                externalServiceClientService.getCropById(1L);

        assertEquals(expectedCrop, result);
    }

    @Test
    void shouldHandleUserServiceFailure() {

        when(userServiceClient.getUserById(1L))
                .thenThrow(
                        new ExternalServiceException(
                                "User Service failed"));

        assertThrows(
                ExternalServiceException.class,
                () -> externalServiceClientService
                        .getUserById(1L));
    }

    @Test
    void shouldHandleCropServiceFailure() {

        when(cropServiceClient.getCropById(1L))
                .thenThrow(
                        new ExternalServiceException(
                                "Crop Service failed"));

        assertThrows(
                ExternalServiceException.class,
                () -> externalServiceClientService
                        .getCropById(1L));
    }
}