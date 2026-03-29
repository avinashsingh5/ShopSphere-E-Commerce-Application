package com.shopsphere.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
}
