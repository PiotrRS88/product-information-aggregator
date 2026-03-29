package com.example.productaggregator.domain;

import java.math.BigDecimal;

public record PricingData(
        boolean available,
        BigDecimal basePrice,
        BigDecimal discountAmount,
        BigDecimal finalPrice,
        String currency
) {
    public static PricingData unavailable(String currency) {
        return new PricingData(false, null, null, null, currency);
    }
}
