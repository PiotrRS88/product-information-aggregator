package com.example.productaggregator.client;

import com.example.productaggregator.domain.MarketContext;
import com.example.productaggregator.domain.PricingData;

public interface PricingService {
    PricingData getPricing(String productId, MarketContext marketContext, String customerId);
}
