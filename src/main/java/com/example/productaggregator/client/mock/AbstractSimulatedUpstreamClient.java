package com.example.productaggregator.client.mock;

import com.example.productaggregator.config.AggregatorProperties;
import com.example.productaggregator.exception.UpstreamServiceException;
import java.util.concurrent.ThreadLocalRandom;

abstract class AbstractSimulatedUpstreamClient {

    private final String serviceName;
    private final AggregatorProperties.ServiceProperties profile;

    protected AbstractSimulatedUpstreamClient(String serviceName, AggregatorProperties properties) {
        this.serviceName = serviceName;
        this.profile = properties.getServices().get(serviceName);
        if (this.profile == null) {
            throw new IllegalStateException("Missing service config for " + serviceName);
        }
    }

    protected void simulateUpstreamBehavior() {
        sleepWithJitter();
        randomFail();
    }

    protected int timeoutMs() {
        return profile.getTimeoutMs();
    }

    private void sleepWithJitter() {
        int jitter = ThreadLocalRandom.current().nextInt(0, 31);
        try {
            Thread.sleep(profile.getLatencyMs() + jitter);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new UpstreamServiceException(serviceName, "Interrupted while waiting for upstream");
        }
    }

    private void randomFail() {
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll > profile.getReliability()) {
            throw new UpstreamServiceException(serviceName, "Upstream service failed");
        }
    }
}
