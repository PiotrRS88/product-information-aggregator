package com.example.productaggregator.domain;

public record ProductView(
        String productId,
        CatalogData catalog,
        PricingData pricing,
        AvailabilityData availability,
        CustomerContextData customerContext
) {
}
