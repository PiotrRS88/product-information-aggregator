package com.example.productaggregator.service;

import com.example.productaggregator.config.MarketResolver;
import com.example.productaggregator.domain.AggregatedProductResponse;
import com.example.productaggregator.domain.MarketContext;
import com.example.productaggregator.service.mapper.ProductResponseAssembler;
import com.example.productaggregator.service.model.ProductEnrichmentResult;
import org.springframework.stereotype.Service;

@Service
public class ProductAggregationService {

    private final MarketResolver marketResolver;
    private final ProductEnrichmentFetcher productEnrichmentFetcher;
    private final ProductResponseAssembler productResponseAssembler;

    public ProductAggregationService(
            MarketResolver marketResolver,
            ProductEnrichmentFetcher productEnrichmentFetcher,
            ProductResponseAssembler productResponseAssembler
    ) {
        this.marketResolver = marketResolver;
        this.productEnrichmentFetcher = productEnrichmentFetcher;
        this.productResponseAssembler = productResponseAssembler;
    }

    public AggregatedProductResponse getProduct(String productId, String marketCode, String customerId) {
        MarketContext marketContext = marketResolver.resolve(marketCode);
        ProductEnrichmentResult enrichmentResult = productEnrichmentFetcher.fetchProduct(productId, marketContext, customerId);

        return productResponseAssembler.buildResponse(
                productId,
                marketContext,
                enrichmentResult.catalog(),
                enrichmentResult.pricing(),
                enrichmentResult.availability(),
                enrichmentResult.customerContext(),
                enrichmentResult.warnings()
        );
    }
}
