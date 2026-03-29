package com.example.productaggregator.api;

import com.example.productaggregator.domain.AggregatedProductResponse;
import com.example.productaggregator.service.ProductAggregationService;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/products")
public class ProductController {

    private final ProductAggregationService productAggregationService;

    public ProductController(ProductAggregationService productAggregationService) {
        this.productAggregationService = productAggregationService;
    }

    @GetMapping("/{productId}")
    public AggregatedProductResponse getProduct(
            @PathVariable String productId,
            @RequestParam @Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$", message = "market must match pattern xx-XX, for example pl-PL") String market,
            @RequestParam(required = false) String customerId
    ) {
        return productAggregationService.getProduct(productId, market, customerId);
    }
}
