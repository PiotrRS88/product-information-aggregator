package com.example.productaggregator.config;

import com.example.productaggregator.domain.MarketContext;
import com.example.productaggregator.exception.InvalidMarketException;
import org.springframework.stereotype.Component;

@Component
public class MarketResolver {

    private final AggregatorProperties properties;

    public MarketResolver(AggregatorProperties properties) {
        this.properties = properties;
    }

    public MarketContext resolve(String marketCode) {
        AggregatorProperties.MarketProperties market = properties.getMarkets().get(marketCode);
        if (market == null) {
            throw new InvalidMarketException("Unsupported market: " + marketCode);
        }
        return new MarketContext(marketCode, market.getLanguage(), market.getCurrency());
    }
}
