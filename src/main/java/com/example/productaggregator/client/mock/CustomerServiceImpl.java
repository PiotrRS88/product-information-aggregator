package com.example.productaggregator.client.mock;

import com.example.productaggregator.client.CustomerService;
import com.example.productaggregator.config.AggregatorProperties;
import com.example.productaggregator.domain.CustomerContextData;
import com.example.productaggregator.domain.MarketContext;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceImpl extends AbstractSimulatedUpstreamClient implements CustomerService {

    private static final String SEGMENT_STANDARD = "standard";
    private static final String SEGMENT_DEALER = "dealer";
    private static final String SEGMENT_DEALER_VIP = "dealer-vip";

    private static final String VIP_SUFFIX = "vip";
    private static final String DEALER_PREFIX = "dealer";

    public CustomerServiceImpl(AggregatorProperties properties) {
        super("customer", properties);
    }

    @Override
    public CustomerContextData getCustomerContext(String customerId, MarketContext marketContext) {
        simulateUpstreamBehavior();

        String segment = resolveSegment(customerId);

        return new CustomerContextData(
                true,
                customerId,
                segment,
                preferencesFor(segment, marketContext.marketCode())
        );
    }

    private String resolveSegment(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return SEGMENT_STANDARD;
        }

        String normalizedCustomerId = customerId.toLowerCase(Locale.ROOT);

        if (normalizedCustomerId.endsWith(VIP_SUFFIX)) {
            return SEGMENT_DEALER_VIP;
        }
        if (normalizedCustomerId.startsWith(DEALER_PREFIX)) {
            return SEGMENT_DEALER;
        }
        return SEGMENT_STANDARD;
    }

    private List<String> preferencesFor(String segment, String marketCode) {
        if (SEGMENT_DEALER_VIP.equals(segment)) {
            return switch (marketCode) {
                case "pl-PL" -> List.of("priority-shipping", "pln-pricing", "dedicated-support");
                case "de-DE" -> List.of("priority-shipping", "eur-pricing", "invoice-payment");
                case "nl-NL" -> List.of("priority-shipping", "eur-pricing", "compact-packaging");
                default -> List.of("priority-shipping");
            };
        }

        if (SEGMENT_DEALER.equals(segment)) {
            return switch (marketCode) {
                case "pl-PL" -> List.of("fast-delivery", "pln-pricing");
                case "de-DE" -> List.of("invoice-payment", "eur-pricing");
                case "nl-NL" -> List.of("compact-packaging", "eur-pricing");
                default -> List.of("standard-offer");
            };
        }

        return List.of("standard-offer");
    }
}
