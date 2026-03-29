package com.example.productaggregator.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "aggregator")
public class AggregatorProperties {

    @Min(0)
    private long timeoutBufferMs;

    @Valid
    @NotEmpty
    private Map<String, MarketProperties> markets;

    @Valid
    @NotEmpty
    private Map<String, ServiceProperties> services;

    @Getter
    @Setter
    public static class MarketProperties {

        @NotBlank
        private String language;

        @NotBlank
        private String currency;

    }

    @Getter
    @Setter
    public static class ServiceProperties {

        @Min(1)
        private int latencyMs;

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private double reliability;

        @Min(1)
        private int timeoutMs;

    }
}
