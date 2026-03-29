package com.example.productaggregator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.productaggregator.client.AvailabilityService;
import com.example.productaggregator.client.CatalogService;
import com.example.productaggregator.client.CustomerService;
import com.example.productaggregator.client.PricingService;
import com.example.productaggregator.client.UpstreamClients;
import com.example.productaggregator.config.AggregatorProperties;
import com.example.productaggregator.config.MarketResolver;
import com.example.productaggregator.domain.AggregatedProductResponse;
import com.example.productaggregator.domain.AvailabilityData;
import com.example.productaggregator.domain.AvailabilityStatus;
import com.example.productaggregator.domain.CatalogData;
import com.example.productaggregator.domain.CustomerContextData;
import com.example.productaggregator.domain.PricingData;
import com.example.productaggregator.domain.WarningMessage;
import com.example.productaggregator.exception.CatalogUnavailableException;
import com.example.productaggregator.exception.ProductNotFoundException;
import com.example.productaggregator.service.mapper.ProductResponseAssembler;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

class ProductAggregationServiceTest {

    private ProductAggregationService service;

    @BeforeEach
    void setUp() {
        service = serviceWith(
                catalogClient(),
                pricingClient(),
                availabilityClient(),
                customerClient()
        );
    }

    @Test
    void shouldAggregateAllSectionsWhenUpstreamsSucceed() {
        AggregatedProductResponse response = service.getProduct("P-1", "pl-PL", "CUST-1");

        assertThat(response.product().catalog().name()).isEqualTo("Filtr");
        assertThat(response.product().pricing().available()).isTrue();
        assertThat(response.product().availability().status()).isEqualTo(AvailabilityStatus.IN_STOCK);
        assertThat(response.product().customerContext().personalized()).isTrue();
        assertThat(response.warnings()).isEmpty();
    }

    @Test
    void shouldFailWhenProductIsNotFound() {
        service = serviceWith(
                (productId, marketContext) -> {
                    throw new ProductNotFoundException("Product not found: " + productId);
                },
                pricingClient(),
                availabilityClient(),
                customerClient()
        );

        assertThatThrownBy(() -> service.getProduct("missing-123", "pl-PL", "CUST-1"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found: missing-123");
    }

    @Test
    void shouldReturnStandardCustomerContextWhenCustomerServiceFails() {
        service = serviceWith(
                catalogClient(),
                pricingClient(),
                availabilityClient(),
                (customerId, marketContext) -> {
                    throw new IllegalStateException("customer down");
                }
        );

        AggregatedProductResponse response = service.getProduct("P-1", "pl-PL", "CUST-1");

        assertThat(response.product().customerContext().personalized()).isFalse();
        assertThat(response.product().customerContext().segment()).isEqualTo("standard");
        assertThat(response.warnings()).extracting(WarningMessage::source).contains("customer");
    }

    @Test
    void shouldReturnFallbackWhenPricingFails() {
        service = serviceWith(
                catalogClient(),
                (productId, marketContext, customerId) -> {
                    throw new IllegalStateException("pricing down");
                },
                availabilityClient(),
                customerClient()
        );

        AggregatedProductResponse response = service.getProduct("P-1", "pl-PL", "CUST-1");

        assertThat(response.product().pricing().available()).isFalse();
        assertThat(response.warnings()).extracting(WarningMessage::source).contains("pricing");
    }

    @Test
    void shouldReturnUnknownAvailabilityWhenAvailabilityFails() {
        service = serviceWith(
                catalogClient(),
                pricingClient(),
                (productId, marketContext) -> {
                    throw new IllegalStateException("availability down");
                },
                customerClient()
        );

        AggregatedProductResponse response = service.getProduct("P-1", "pl-PL", "CUST-1");

        assertThat(response.product().availability().status()).isEqualTo(AvailabilityStatus.UNKNOWN);
        assertThat(response.warnings()).extracting(WarningMessage::source).contains("availability");
    }

    @Test
    void shouldReturnStandardCustomerContextWhenCustomerIdMissing() {
        AggregatedProductResponse response = service.getProduct("P-1", "pl-PL", null);

        assertThat(response.product().customerContext().personalized()).isFalse();
        assertThat(response.product().customerContext().segment()).isEqualTo("standard");
    }

    @Test
    void shouldFailWholeRequestWhenCatalogFails() {
        service = serviceWith(
                (productId, marketContext) -> {
                    throw new IllegalStateException("catalog down");
                },
                pricingClient(),
                availabilityClient(),
                customerClient()
        );

        assertThatThrownBy(() -> service.getProduct("P-1", "pl-PL", "CUST-1"))
                .isInstanceOf(CatalogUnavailableException.class);
    }

    private ProductAggregationService serviceWith(
            CatalogService catalogService,
            PricingService pricingService,
            AvailabilityService availabilityService,
            CustomerService customerService
    ) {
        AggregatorProperties properties = properties();
        UpstreamClients upstreamClients = new UpstreamClients(
                catalogService,
                pricingService,
                availabilityService,
                customerService
        );

        ProductEnrichmentFetcher productEnrichmentFetcher = new ProductEnrichmentFetcher(
                upstreamClients,
                properties,
                new SyncTaskExecutor()
        );

        return new ProductAggregationService(
                new MarketResolver(properties),
                productEnrichmentFetcher,
                new ProductResponseAssembler()
        );
    }

    private AggregatorProperties properties() {
        AggregatorProperties properties = new AggregatorProperties();
        properties.setTimeoutBufferMs(10);
        properties.setMarkets(Map.of(
                "pl-PL", market("pl", "PLN")
        ));
        properties.setServices(Map.of(
                "catalog", serviceProfile(10, 1.0, 100),
                "pricing", serviceProfile(10, 1.0, 100),
                "availability", serviceProfile(10, 1.0, 100),
                "customer", serviceProfile(10, 1.0, 100)
        ));
        return properties;
    }

    private CatalogService catalogClient() {
        return (productId, marketContext) -> new CatalogData(
                productId,
                "SKU-1",
                "Filtr",
                "Opis",
                "filters",
                Map.of("diameter_mm", "82"),
                List.of("img-1")
        );
    }

    private PricingService pricingClient() {
        return (productId, marketContext, customerId) -> new PricingData(
                true,
                BigDecimal.valueOf(100),
                BigDecimal.TEN,
                BigDecimal.valueOf(90),
                marketContext.currency()
        );
    }

    private AvailabilityService availabilityClient() {
        return (productId, marketContext) -> new AvailabilityData(
                AvailabilityStatus.IN_STOCK,
                10,
                "Poznan",
                "2025-03-30"
        );
    }

    private CustomerService customerClient() {
        return (customerId, marketContext) -> new CustomerContextData(
                true,
                customerId,
                "dealer",
                List.of("fast-delivery")
        );
    }

    private AggregatorProperties.MarketProperties market(String language, String currency) {
        AggregatorProperties.MarketProperties market = new AggregatorProperties.MarketProperties();
        market.setLanguage(language);
        market.setCurrency(currency);
        return market;
    }

    private AggregatorProperties.ServiceProperties serviceProfile(int latencyMs, double reliability, int timeoutMs) {
        AggregatorProperties.ServiceProperties service = new AggregatorProperties.ServiceProperties();
        service.setLatencyMs(latencyMs);
        service.setReliability(reliability);
        service.setTimeoutMs(timeoutMs);
        return service;
    }
}
