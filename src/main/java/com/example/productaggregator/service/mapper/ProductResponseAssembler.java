package com.example.productaggregator.service.mapper;

import com.example.productaggregator.domain.AggregatedProductResponse;
import com.example.productaggregator.domain.AvailabilityData;
import com.example.productaggregator.domain.CatalogData;
import com.example.productaggregator.domain.CustomerContextData;
import com.example.productaggregator.domain.MarketContext;
import com.example.productaggregator.domain.PricingData;
import com.example.productaggregator.domain.ProductView;
import com.example.productaggregator.domain.ResponseContext;
import com.example.productaggregator.domain.WarningMessage;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductResponseAssembler {

    public AggregatedProductResponse buildResponse(
            String productId,
            MarketContext marketContext,
            CatalogData catalog,
            PricingData pricing,
            AvailabilityData availability,
            CustomerContextData customerContext,
            List<WarningMessage> warnings
    ) {
        return new AggregatedProductResponse(
                new ProductView(
                        productId,
                        catalog,
                        pricing,
                        availability,
                        customerContext
                ),
                new ResponseContext(
                        marketContext.marketCode(),
                        marketContext.language()
                ),
                warnings
        );
    }
}
