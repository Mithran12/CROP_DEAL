package com.cropdeal.report;

import com.cropdeal.report.controller.ReportController;
import com.cropdeal.report.dto.ReportFilterResponse;
import com.cropdeal.report.service.ReportService;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportControllerTest {

	@Test
	void searchReportEndpointWorks() throws Exception {

		ReportService service = mock(ReportService.class);

		ReportFilterResponse response = new ReportFilterResponse();
		response.setTotalOrders(1);

		when(service.searchOrders(any(), any(), any(), any(), any(), any(), any())).thenReturn(response);

		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ReportController(service)).build();

		mvc.perform(get("/reports/orders/search").param("status", "COMPLETED").param("userId", "10").param("location",
				"Dindigul")).andExpect(status().isOk());
	}
}