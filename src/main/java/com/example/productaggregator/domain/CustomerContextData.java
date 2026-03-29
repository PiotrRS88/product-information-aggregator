package com.example.productaggregator.domain;

import java.util.List;

public record CustomerContextData(
        boolean personalized,
        String customerId,
        String segment,
        List<String> preferences
) {
    public static CustomerContextData standard() {
        return new CustomerContextData(false, null, "standard", List.of());
    }
}
