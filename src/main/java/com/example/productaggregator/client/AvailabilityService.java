package com.example.productaggregator.client;

import com.example.productaggregator.domain.AvailabilityData;
import com.example.productaggregator.domain.MarketContext;

public interface AvailabilityService {
    AvailabilityData getAvailability(String productId, MarketContext marketContext);
}
