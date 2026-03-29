package com.example.productaggregator.client.mock;

import com.example.productaggregator.client.AvailabilityService;
import com.example.productaggregator.config.AggregatorProperties;
import com.example.productaggregator.domain.AvailabilityData;
import com.example.productaggregator.domain.AvailabilityStatus;
import com.example.productaggregator.domain.MarketContext;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityServiceImpl extends AbstractSimulatedUpstreamClient implements AvailabilityService {

    public AvailabilityServiceImpl(AggregatorProperties properties) {
        super("availability", properties);
    }

    @Override
    public AvailabilityData getAvailability(String productId, MarketContext marketContext) {
        simulateUpstreamBehavior();
        int stockLevel = Math.abs(productId.hashCode()) % 40;
        AvailabilityStatus status = stockLevel == 0
                ? AvailabilityStatus.OUT_OF_STOCK
                : stockLevel < 5 ? AvailabilityStatus.LOW_STOCK : AvailabilityStatus.IN_STOCK;
        String delivery = status == AvailabilityStatus.OUT_OF_STOCK
                ? LocalDate.now().plusDays(7).toString()
                : LocalDate.now().plusDays(2).toString();
        return new AvailabilityData(status, stockLevel, warehouseForMarket(marketContext.marketCode()), delivery);
    }

    private String warehouseForMarket(String marketCode) {
        return switch (marketCode) {
            case "pl-PL" -> "Modla Krolewska";
            case "de-DE" -> "Strullendorf";
            case "nl-NL" -> "Varsseveld";
            default -> "Central";
        };
    }
}
