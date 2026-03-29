package com.example.productaggregator.client.mock;

import com.example.productaggregator.client.PricingService;
import com.example.productaggregator.config.AggregatorProperties;
import com.example.productaggregator.domain.MarketContext;
import com.example.productaggregator.domain.PricingData;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class PricingServiceImpl extends AbstractSimulatedUpstreamClient implements PricingService {

    public PricingServiceImpl(AggregatorProperties properties) {
        super("pricing", properties);
    }

    @Override
    public PricingData getPricing(String productId, MarketContext marketContext, String customerId) {
        simulateUpstreamBehavior();
        BigDecimal basePrice = switch (marketContext.marketCode()) {
            case "pl-PL" -> BigDecimal.valueOf(129.99);
            case "nl-NL" -> BigDecimal.valueOf(31.49);
            default -> BigDecimal.valueOf(29.99);
        };
        BigDecimal discount = customerId == null ? BigDecimal.ZERO : basePrice.multiply(BigDecimal.valueOf(0.08));
        BigDecimal finalPrice = basePrice.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        return new PricingData(
                true,
                basePrice.setScale(2, RoundingMode.HALF_UP),
                discount.setScale(2, RoundingMode.HALF_UP),
                finalPrice,
                marketContext.currency()
        );
    }
}
