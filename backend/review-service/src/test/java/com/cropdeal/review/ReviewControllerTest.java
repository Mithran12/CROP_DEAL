package com.cropdeal.review;

import com.cropdeal.review.controller.ReviewController;
import com.cropdeal.review.dto.ReviewResponse;
import com.cropdeal.review.service.ReviewService;

import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest {

    @Test
    void createReviewEndpointWorks() throws Exception {

        ReviewService service = mock(ReviewService.class);

        ReviewResponse response = new ReviewResponse();
        response.setRating(5);

        when(service.create(any())).thenReturn(response);

        ReviewController controller = new ReviewController(service);

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        mvc.perform(
                post("/reviews")
                        .contentType(APPLICATION_JSON)
                        .content("{\"orderId\":1,\"rating\":5,\"review\":\"Good\"}")
        )
        .andExpect(status().isCreated());
    }
}
