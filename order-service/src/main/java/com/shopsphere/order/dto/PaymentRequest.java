package com.shopsphere.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "Payment mode is required")
    private String paymentMode; // CARD, UPI, COD
}
