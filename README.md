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

The solution is intentionally split into a few focused components. `ProductAggregationService` coordinates the use case, `MarketResolver` resolves market-specific context, `ProductEnrichmentFetcher` handles parallel upstream calls, timeout and fallback policies, and the required-vs-optional dependency distinction, while `ProductResponseAssembler` builds the final API response. This keeps orchestration readable and prevents transport logic, fallback rules, and response assembly from being mixed in one class.

Simulated upstream services are implemented behind interfaces. Each simulated service adds latency jitter and occasional failures based on the assignment’s reliability numbers. This makes the service behavior closer to a distributed system while keeping the project lightweight and easy to run locally.

The API contract is client-oriented. Instead of failing the whole response when optional dependencies are unavailable, the service returns a usable payload plus explicit warnings. Clients do not need to infer degradation from unexplained `null` values.

Domain DTOs and value objects are modeled as Java records. Lombok is used selectively for configuration boilerplate where it reduces noise without obscuring intent.

## Trade-offs

I used in-process simulated upstream clients instead of separate mock HTTP services. This keeps the focus on orchestration, resilience rules and code quality. In a fuller version I would expose those simulators as separate services or use tools such as WireMock or Testcontainers to model network boundaries more realistically.

I used `CompletableFuture` rather than a full reactive stack. The assignment needs parallel I/O, timeouts and graceful degradation, but does not require the complexity of end-to-end reactive programming.


## Production Readiness

- Per-upstream timeout configuration in `application.yml`
- Explicit fallback behavior for optional dependencies
- Structured logging around degraded upstream calls
- Market and service profiles externalized in configuration
- Health and metrics endpoints can be exposed through Spring Boot Actuator

## Design Question: Option C

If the platform expands to 5 new markets, the main changes in the current design would be configuration and market-specific data rather than core architecture. Market handling is already centralized through `MarketResolver` and externalized configuration, so adding new markets would mostly mean defining new market codes, languages and currencies.

Some simulated upstream logic would also need updates where mock responses currently depend on market-specific values, such as warehouse location or localized sample data. The aggregation flow itself would not need to change, because it is already independent of the number of supported markets.

With more time, I would move market definitions into a dedicated configuration source or reference dataset to make adding new markets safer and easier to operate.
