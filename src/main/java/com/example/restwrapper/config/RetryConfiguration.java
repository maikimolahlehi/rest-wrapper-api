package com.example.restwrapper.config;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RetryConfiguration {
    private final RestWrapperConfig restWrapperConfig;

    public RetryConfiguration(final RestWrapperConfig restWrapperConfig) {
        this.restWrapperConfig = restWrapperConfig;
    }

    @Bean
    public RetryConfig getRetryConfig() {
        final IntervalFunction intervalWithExponentialBackoff = IntervalFunction
                .ofExponentialBackoff(Duration.ofSeconds(restWrapperConfig.getDelayBeforeRetry()),
                        restWrapperConfig.getRetryMultiplier());
        return RetryConfig
                .custom()
                .maxAttempts(restWrapperConfig.getRetryAttempts())
                .intervalFunction(intervalWithExponentialBackoff)
                .writableStackTraceEnabled(restWrapperConfig.isWritableStackTraceEnabled())
                .build();
    }

    @Bean
    public RetryRegistry getRetryRegistry() {
        return RetryRegistry.of(getRetryConfig());
    }
}
