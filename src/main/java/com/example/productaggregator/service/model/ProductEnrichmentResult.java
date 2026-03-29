package com.example.productaggregator.service.model;

import com.example.productaggregator.domain.AvailabilityData;
import com.example.productaggregator.domain.CatalogData;
import com.example.productaggregator.domain.CustomerContextData;
import com.example.productaggregator.domain.PricingData;
import com.example.productaggregator.domain.WarningMessage;
import java.util.List;

public record ProductEnrichmentResult(
        CatalogData catalog,
        PricingData pricing,
        AvailabilityData availability,
        CustomerContextData customerContext,
        List<WarningMessage> warnings
) {
}
