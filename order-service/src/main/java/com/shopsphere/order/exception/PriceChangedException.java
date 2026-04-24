package com.shopsphere.order.exception;

public class PriceChangedException extends RuntimeException {

    public PriceChangedException(String message) {
        super(message);
    }
}
