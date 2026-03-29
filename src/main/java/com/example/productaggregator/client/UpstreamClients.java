package com.example.productaggregator.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpstreamClients {

    private final CatalogService catalogService;
    private final PricingService pricingService;
    private final AvailabilityService availabilityService;
    private final CustomerService customerService;

    public CatalogService catalog() {
        return catalogService;
    }

    public PricingService pricing() {
        return pricingService;
    }

    public AvailabilityService availability() {
        return availabilityService;
    }

    public CustomerService customer() {
        return customerService;
    }
}
