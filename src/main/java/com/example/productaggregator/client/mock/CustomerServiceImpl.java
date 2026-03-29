package com.example.productaggregator.client.mock;

import com.example.productaggregator.client.CustomerService;
import com.example.productaggregator.config.AggregatorProperties;
import com.example.productaggregator.domain.CustomerContextData;
import com.example.productaggregator.domain.MarketContext;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceImpl extends AbstractSimulatedUpstreamClient implements CustomerService {

    public CustomerServiceImpl(AggregatorProperties properties) {
        super("customer", properties);
    }

    @Override
    public CustomerContextData getCustomerContext(String customerId, MarketContext marketContext) {
        simulateUpstreamBehavior();
        return new CustomerContextData(
                true,
                customerId,
                customerId.endsWith("VIP") ? "dealer-vip" : "dealer",
                preferencesForMarket(marketContext.marketCode())
        );
    }

    private List<String> preferencesForMarket(String marketCode) {
        return switch (marketCode) {
            case "pl-PL" -> List.of("fast-delivery", "pln-pricing");
            case "de-DE" -> List.of("invoice-payment", "eur-pricing");
            case "nl-NL" -> List.of("compact-packaging", "eur-pricing");
            default -> List.of("standard-offer");
        };
    }
}
