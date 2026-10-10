package com.cropdeal.crop;

import com.cropdeal.crop.controller.CropController;
import com.cropdeal.crop.dto.CropResponse;
import com.cropdeal.crop.entity.CropStatus;
import com.cropdeal.crop.service.CropService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CropControllerAdvancedFilterTest {

    @Test
    void advancedFilterAcceptsCombinations() throws Exception {

        CropService service = Mockito.mock(CropService.class);

        Mockito.when(service.advancedFilter(
                eq("Rice"),
                eq("Grain"),
                eq("Dindigul"),
                eq("A"),
                eq(10L),
                eq(CropStatus.AVAILABLE)
        )).thenReturn(List.of(new CropResponse()));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new CropController(service))
                .build();

        mockMvc.perform(
                get("/crops/filter/advanced")
                        .param("name", "Rice")
                        .param("category", "Grain")
                        .param("location", "Dindigul")
                        .param("grade", "A")
                        .param("farmerId", "10")
                        .param("status", "AVAILABLE")
        ).andExpect(status().isOk());
    }
}
