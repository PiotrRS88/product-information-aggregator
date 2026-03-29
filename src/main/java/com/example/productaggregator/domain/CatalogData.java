package com.example.productaggregator.domain;

import java.util.List;
import java.util.Map;

public record CatalogData(
        String productId,
        String sku,
        String name,
        String description,
        String category,
        Map<String, String> specifications,
        List<String> images
) {
}
