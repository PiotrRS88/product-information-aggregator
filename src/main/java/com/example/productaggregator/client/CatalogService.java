package com.example.productaggregator.client;

import com.example.productaggregator.domain.CatalogData;
import com.example.productaggregator.domain.MarketContext;

public interface CatalogService {
    CatalogData getProduct(String productId, MarketContext marketContext);
}
