# Product Information Aggregator

Spring Boot service that aggregates product data from multiple internal domains into a single market-aware response. It simulates a distributed system with latency, occasional failures, required vs optional dependencies, and graceful degradation.

## Requirements

- Java 21+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

Example request:

```bash
curl "http://localhost:8080/v1/products/P-100?market=pl-PL&customerId=DEALER-1"
```

## Test

```bash
mvn test
```

## API

`GET /v1/products/{productId}?market=pl-PL&customerId=DEALER-1`

### Query parameters

- `market` is required and must match `xx-XX`
- `customerId` is optional

### Behavior

- Catalog technical failure fails the whole request
- Missing product returns `404 Not Found`
- Pricing failure returns `pricing.available=false`
- Availability failure returns `availability.status=UNKNOWN`
- Customer failure or missing `customerId` returns standard, non-personalized context

## Design Decisions

The solution is intentionally split into a few focused components. `ProductAggregationService` coordinates the use case, `MarketResolver` resolves market-specific context, `ProductEnrichmentFetcher` handles parallel upstream calls and fallback behavior, and `ProductResponseAssembler` builds the final API response. This keeps orchestration readable and makes the required-vs-optional distinction explicit in code.

Simulated upstream services are implemented behind interfaces. Each simulated service adds latency jitter and occasional failures based on the assignment’s reliability numbers. This makes the service behavior closer to a distributed system while keeping the project lightweight and easy to run locally.

The API contract is client-oriented. Instead of failing the whole response when optional dependencies are unavailable, the service returns a usable payload plus explicit warnings. Clients do not need to infer degradation from unexplained `null` values.

Domain DTOs and value objects are modeled as Java records. Lombok is used selectively for configuration boilerplate where it reduces noise without obscuring intent.

## Trade-offs

I used in-process simulated upstream clients instead of separate mock HTTP services. For a 3-4 hour assignment this keeps the focus on orchestration, resilience rules and code quality. In a fuller version I would expose those simulators as separate services or use tools such as WireMock or Testcontainers to model network boundaries more realistically.

I used `CompletableFuture` rather than a full reactive stack. The assignment needs parallel I/O, timeouts and graceful degradation, but does not require the complexity of end-to-end reactive programming.

## Production Readiness

- Per-upstream timeout configuration in `application.yml`
- Explicit fallback behavior for optional dependencies
- Structured logging around degraded upstream calls
- Market and service profiles externalized in configuration
- Health and metrics endpoints can be exposed through Spring Boot Actuator

With more time I would add:

- Resilience4j circuit breakers and bulkheads
- Request correlation IDs and richer per-upstream metrics
- Separate HTTP adapters for upstream integrations
- OpenAPI documentation
- More tests around timeout paths and degraded responses

## Design Question: Option A

If the Assortment team adds a `Related Products` service with `200ms` latency and `90%` reliability, I would add it as another optional enricher behind its own client interface. Its reliability is too low, and its latency is too high, to make it required for a product detail page. The response should still be useful without it.

In this design that change is small: add a new client interface, a simulated implementation, a response section, and one more asynchronous call in the enrichment layer. The existing structure already separates required and optional dependencies, so no major refactor is needed.