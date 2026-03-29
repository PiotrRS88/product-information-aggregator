package com.example.productaggregator.client;

import com.example.productaggregator.domain.CustomerContextData;
import com.example.productaggregator.domain.MarketContext;

public interface CustomerService {
    CustomerContextData getCustomerContext(String customerId, MarketContext marketContext);
}
