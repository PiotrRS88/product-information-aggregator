package com.example.productaggregator.domain;

import java.util.List;

public record AggregatedProductResponse(
        ProductView product,
        ResponseContext context,
        List<WarningMessage> warnings
) {
}
