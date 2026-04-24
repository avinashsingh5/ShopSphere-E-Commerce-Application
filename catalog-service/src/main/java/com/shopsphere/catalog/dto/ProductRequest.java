package com.shopsphere.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create or update a product")
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Schema(description = "Name of the product", example = "Wireless Bluetooth Headphones")
    private String name;

    @Schema(description = "Description of the product", example = "Premium noise-cancelling wireless headphones")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(description = "Price of the product", example = "2499.00")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock must be zero or positive")
    @Schema(description = "Available stock quantity", example = "50")
    private Integer stockQuantity;

    @Schema(description = "URL of the product image", example = "https://example.com/headphones.jpg")
    private String imageUrl;

    @Schema(description = "Whether the product is featured", example = "true")
    private boolean featured;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be valid")
    @Schema(description = "Category ID of the product", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long categoryId;
}
