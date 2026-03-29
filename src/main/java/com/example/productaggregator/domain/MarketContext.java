package com.example.productaggregator.domain;

public record MarketContext(
        String marketCode,
        String language,
        String currency
) {
}
