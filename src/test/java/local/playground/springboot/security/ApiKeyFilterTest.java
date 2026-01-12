package local.playground.springboot.security;

import local.playground.springboot.workorders.controller.WorkOrderController;
import local.playground.springboot.workorders.service.WorkOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiKeyFilterTest {

    private MockMvc mockMvc;

    private WorkOrderService service;

    private ApiKeyFilter apiKeyFilter;

    @BeforeEach
    void setup() throws Exception {
        service = Mockito.mock(WorkOrderService.class);
        apiKeyFilter = new ApiKeyFilter();
        // inject expectedApiKey value used by the filter
        Field f = ApiKeyFilter.class.getDeclaredField("expectedApiKey");
        f.setAccessible(true);
        f.set(apiKeyFilter, "secret123");

        mockMvc = MockMvcBuilders.standaloneSetup(new WorkOrderController(service))
                .addFilters(apiKeyFilter)
                .build();
    }

    @Test
    void testMissingApiKey() throws Exception {
        mockMvc.perform(get("/work-orders/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testValidApiKey() throws Exception {
        Mockito.when(service.getStatusCounts())
                .thenReturn(Map.of("completed", 1L, "pending", 2L));

        mockMvc.perform(get("/work-orders/status")
                        .header("X-API-KEY", "secret123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.pending").value(2));
    }
}
