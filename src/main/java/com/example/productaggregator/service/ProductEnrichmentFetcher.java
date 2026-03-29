package com.example.productaggregator.service;

import com.example.productaggregator.client.UpstreamClients;
import com.example.productaggregator.config.AggregatorProperties;
import com.example.productaggregator.domain.AvailabilityData;
import com.example.productaggregator.domain.CatalogData;
import com.example.productaggregator.domain.CustomerContextData;
import com.example.productaggregator.domain.MarketContext;
import com.example.productaggregator.domain.PricingData;
import com.example.productaggregator.domain.WarningMessage;
import com.example.productaggregator.exception.CatalogUnavailableException;
import com.example.productaggregator.exception.ProductNotFoundException;
import com.example.productaggregator.service.model.OptionalEnrichment;
import com.example.productaggregator.service.model.ProductEnrichmentResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ProductEnrichmentFetcher {

    private static final Logger logger = LoggerFactory.getLogger(ProductEnrichmentFetcher.class);

    private final UpstreamClients upstreamClients;
    private final AggregatorProperties properties;
    private final TaskExecutor aggregationExecutor;

    public ProductEnrichmentFetcher(
            UpstreamClients upstreamClients,
            AggregatorProperties properties,
            @Qualifier("aggregationExecutor") TaskExecutor aggregationExecutor
    ) {
        this.upstreamClients = upstreamClients;
        this.properties = properties;
        this.aggregationExecutor = aggregationExecutor;
    }

    public ProductEnrichmentResult fetchProduct(String productId, MarketContext marketContext, String customerId) {
        CompletableFuture<CatalogData> catalogFuture = fetchRequiredCatalog(productId, marketContext);
        CompletableFuture<OptionalEnrichment<PricingData>> pricingFuture = fetchPricing(productId, marketContext, customerId);
        CompletableFuture<OptionalEnrichment<AvailabilityData>> availabilityFuture = fetchAvailability(productId, marketContext);
        CompletableFuture<OptionalEnrichment<CustomerContextData>> customerFuture = fetchCustomerContext(customerId, marketContext);

        CatalogData catalog = joinRequiredCatalog(catalogFuture, productId, marketContext.marketCode());
        OptionalEnrichment<PricingData> pricing = pricingFuture.join();
        OptionalEnrichment<AvailabilityData> availability = availabilityFuture.join();
        OptionalEnrichment<CustomerContextData> customer = customerFuture.join();

        return new ProductEnrichmentResult(
                catalog,
                pricing.data(),
                availability.data(),
                customer.data(),
                collectWarnings(pricing, availability, customer)
        );
    }

    private CompletableFuture<CatalogData> fetchRequiredCatalog(String productId, MarketContext marketContext) {
        return requiredCall(
                () -> upstreamClients.catalog().getProduct(productId, marketContext),
                "catalog"
        );
    }

    private CompletableFuture<OptionalEnrichment<PricingData>> fetchPricing(
            String productId,
            MarketContext marketContext,
            String customerId
    ) {
        return optionalCall(
                () -> upstreamClients.pricing().getPricing(productId, marketContext, customerId),
                PricingData.unavailable(marketContext.currency()),
                "pricing",
                "Pricing is unavailable"
        );
    }

    private CompletableFuture<OptionalEnrichment<AvailabilityData>> fetchAvailability(
            String productId,
            MarketContext marketContext
    ) {
        return optionalCall(
                () -> upstreamClients.availability().getAvailability(productId, marketContext),
                AvailabilityData.unknown(),
                "availability",
                "Availability is unknown"
        );
    }

    private CompletableFuture<OptionalEnrichment<CustomerContextData>> fetchCustomerContext(
            String customerId,
            MarketContext marketContext
    ) {
        if (customerId == null || customerId.isBlank()) {
            return CompletableFuture.completedFuture(
                    OptionalEnrichment.success(CustomerContextData.standard())
            );
        }

        return optionalCall(
                () -> upstreamClients.customer().getCustomerContext(customerId, marketContext),
                CustomerContextData.standard(),
                "customer",
                "Personalized customer context is unavailable"
        );
    }

    private CatalogData joinRequiredCatalog(CompletableFuture<CatalogData> future, String productId, String marketCode) {
        try {
            return future.join();
        } catch (CompletionException exception) {
            Throwable cause = unwrap(exception);
            if (cause instanceof ProductNotFoundException productNotFoundException) {
                throw productNotFoundException;
            }
            logger.warn("Catalog lookup failed for productId={} market={}: {}", productId, marketCode, cause.getMessage());
            throw new CatalogUnavailableException("Catalog service is required but unavailable", cause);
        }
    }

    private <T> CompletableFuture<T> requiredCall(Supplier<T> supplier, String serviceName) {
        return CompletableFuture.supplyAsync(supplier, aggregationExecutor)
                .orTimeout(timeoutFor(serviceName), TimeUnit.MILLISECONDS);
    }

    private <T> CompletableFuture<OptionalEnrichment<T>> optionalCall(
            Supplier<T> supplier,
            T fallback,
            String serviceName,
            String warningMessage
    ) {
        return CompletableFuture.supplyAsync(supplier, aggregationExecutor)
                .orTimeout(timeoutFor(serviceName), TimeUnit.MILLISECONDS)
                .handle((value, throwable) -> {
                    if (throwable == null) {
                        return OptionalEnrichment.success(value);
                    }

                    Throwable cause = unwrap(throwable);
                    logger.info("Optional upstream {} degraded: {}", serviceName, cause.getMessage());

                    return OptionalEnrichment.failure(
                            fallback,
                            new WarningMessage(serviceName, warningMessage)
                    );
                });
    }

    private List<WarningMessage> collectWarnings(OptionalEnrichment<?>... enrichments) {
        List<WarningMessage> warnings = new ArrayList<>();
        for (OptionalEnrichment<?> enrichment : enrichments) {
            enrichment.warning().ifPresent(warnings::add);
        }
        return warnings;
    }

    private long timeoutFor(String serviceName) {
        AggregatorProperties.ServiceProperties service = properties.getServices().get(serviceName);
        return service.getTimeoutMs() + properties.getTimeoutBufferMs();
    }

    private Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            return completionException.getCause();
        }
        return throwable;
    }
}
