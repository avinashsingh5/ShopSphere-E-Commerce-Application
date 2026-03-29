package com.shopsphere.admin.controller;

import com.shopsphere.admin.dto.DashboardResponse;
import com.shopsphere.admin.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    void getDashboard_ShouldReturn200() throws Exception {
        DashboardResponse response = DashboardResponse.builder()
                .totalProducts(50).totalOrders(100).pendingOrders(10)
                .deliveredOrders(80).cancelledOrders(10).build();
        when(dashboardService.getDashboardMetrics(anyString(), anyString())).thenReturn(response);

        mockMvc.perform(get("/admin/dashboard")
                        .header("X-User-Name", "admin@test.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(50))
                .andExpect(jsonPath("$.totalOrders").value(100))
                .andExpect(jsonPath("$.pendingOrders").value(10))
                .andExpect(jsonPath("$.deliveredOrders").value(80));
    }
}
