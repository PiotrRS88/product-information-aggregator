package com.example.productaggregator.service.model;

import com.example.productaggregator.domain.WarningMessage;

import java.util.Optional;

public record OptionalEnrichment<T>(T data, Optional<WarningMessage> warning) {
    public static <T> OptionalEnrichment<T> success(T data) {
        return new OptionalEnrichment<>(data, Optional.empty());
    }

    public static <T> OptionalEnrichment<T> failure(T data, WarningMessage warning) {
        return new OptionalEnrichment<>(data, Optional.of(warning));
    }
}
