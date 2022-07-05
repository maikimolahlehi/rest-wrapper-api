package com.example.restwrapper.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {

    private final RestWrapperConfig restWrapperConfig;

    public CircuitBreakerConfiguration(final RestWrapperConfig restWrapperConfig) {
        this.restWrapperConfig = restWrapperConfig;
    }

    @Bean
    public CircuitBreakerConfig getCircuitBreakerConfig() {
        return CircuitBreakerConfig
                .custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.valueOf(restWrapperConfig.getSlidingWindowType()))
                .slidingWindowSize(restWrapperConfig.getSlidingWindowSize())
                .failureRateThreshold(restWrapperConfig.getFailureRateThreshold())
                .slowCallRateThreshold(restWrapperConfig.getSlowCallRateThreshold())
                .slowCallDurationThreshold(Duration.ofSeconds(restWrapperConfig.getSlowCallDurationThreshold()))
                .permittedNumberOfCallsInHalfOpenState(restWrapperConfig.getPermittedNumberOfCallsInHalfOpenState())
                .minimumNumberOfCalls(restWrapperConfig.getMinimumNumberOfCalls())
                .maxWaitDurationInHalfOpenState(Duration.ofSeconds(restWrapperConfig.getMaxWaitDurationInHalfOpenState()))
                .waitDurationInOpenState(Duration.ofSeconds(restWrapperConfig.getWaitDurationInOpenState()))
                .automaticTransitionFromOpenToHalfOpenEnabled(restWrapperConfig.isAutomaticTransitionFromOpenToHalfOpenEnabled())
                .writableStackTraceEnabled(restWrapperConfig.isWritableStackTraceEnabled())
                .build();
    }

    @Bean
    public CircuitBreakerRegistry getCircuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(getCircuitBreakerConfig());
    }
}
