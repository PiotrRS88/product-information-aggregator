package com.example.productaggregator.domain;

public record AvailabilityData(
        AvailabilityStatus status,
        Integer stockLevel,
        String warehouseLocation,
        String expectedDelivery
) {
    public static AvailabilityData unknown() {
        return new AvailabilityData(AvailabilityStatus.UNKNOWN, null, null, null);
    }
}
