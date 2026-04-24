package com.shopsphere.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Internal DTO representing trusted product data fetched from Catalog Service.
 * Never exposed to external clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInfo {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
}
