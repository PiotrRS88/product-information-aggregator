package com.example.productaggregator.client.mock;

import com.example.productaggregator.client.CatalogService;
import com.example.productaggregator.config.AggregatorProperties;
import com.example.productaggregator.domain.CatalogData;
import com.example.productaggregator.domain.MarketContext;
import java.util.List;
import java.util.Map;

import com.example.productaggregator.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CatalogServiceImpl extends AbstractSimulatedUpstreamClient implements CatalogService {

    public CatalogServiceImpl(AggregatorProperties properties) {
        super("catalog", properties);
    }

    @Override
    public CatalogData getProduct(String productId, MarketContext marketContext) {
        simulateUpstreamBehavior();
        if (productId.startsWith("missing")) {
            throw new ProductNotFoundException("Product not found: " + productId);
        }
        return new CatalogData(
                productId,
                "SKU-" + productId,
                localizedName(marketContext.language()),
                localizedDescription(marketContext.language()),
                "filters",
                Map.of(
                        "material", "cellulose",
                        "diameter_mm", "82",
                        "height_mm", "76"
                ),
                List.of(
                        "https://cdn.example.com/products/" + productId + "/1.jpg",
                        "https://cdn.example.com/products/" + productId + "/2.jpg"
                )
        );
    }

    private String localizedName(String language) {
        return switch (language) {
            case "pl" -> "Filtr oleju premium";
            case "de" -> "Premium-Oelfilter";
            case "nl" -> "Premium oliefilter";
            default -> "Premium oil filter";
        };
    }

    private String localizedDescription(String language) {
        return switch (language) {
            case "pl" -> "Wysokowydajny filtr oleju do maszyn rolniczych.";
            case "de" -> "Leistungsstarker Oelfilter fuer Landmaschinen.";
            case "nl" -> "Hoogwaardige oliefilter voor landbouwmachines.";
            default -> "High-performance oil filter for agricultural machinery.";
        };
    }
}
