package com.example.restwrapper.services;

import com.example.restwrapper.config.RestWrapperConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.EventConsumer;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/*
    documentation of this can be found here:
    https://resilience4j.readme.io/docs/circuitbreaker
    https://resilience4j.readme.io/docs/retry
 */
@Service
@Slf4j
class RestWrapperImpl implements RestWrapper {

    private final RestWrapperConfig restWrapperConfig;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public RestWrapperImpl(final RestWrapperConfig restWrapperConfig,
                           final CircuitBreakerRegistry circuitBreakerRegistry,
                           final RetryRegistry retryRegistry) {
        this.restWrapperConfig = restWrapperConfig;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @Override
    public <T> T executeRestWithRetry(final Supplier<T> supplier,
                                         final String serviceName) {
        final Retry retry = buildRetry(serviceName);
        final Supplier<T> retryingSupplier = Retry.decorateSupplier(retry, supplier);
        return retryingSupplier.get();
    }

    @Override
    public <T> T executeRestWithCircuitBreaker(Supplier<T> supplier, String serviceName) {
        final CircuitBreaker circuitBreaker = buildCircuitBreaker(serviceName);
        final Supplier<T> circuitBreakerSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        return circuitBreakerSupplier.get();
    }

    @Override
    public <T> T executeRestWithCircuitBreaker(final Supplier<T> supplier,
                                                  final Function<Throwable, T> fallBack,
                                                  final String serviceName) {
        final CircuitBreaker circuitBreaker = buildCircuitBreaker(serviceName);
        final Supplier<T> circuitBreakerSupplier = Decorators
                .ofSupplier(supplier)
                .withCircuitBreaker(circuitBreaker)
                .withFallback(fallBack)
                .decorate();
        return circuitBreakerSupplier.get();
    }

    @Override
    public <T> T executeRestWithCircuitBreakerAndRetry(Supplier<T> supplier, String serviceName) {
        final Retry retry = buildRetry(serviceName);
        final CircuitBreaker circuitBreaker = buildCircuitBreaker(serviceName);
        final Supplier<T> decoratedSupplier = Decorators
                .ofSupplier(supplier)
                .withRetry(retry)
                .withCircuitBreaker(circuitBreaker)
                .decorate();
        return decoratedSupplier.get();
    }

    @Override
    public <T> T executeRestWithCircuitBreakerAndRetry(final Supplier<T> supplier,
                                                          final Function<Throwable, T> fallBack,
                                                          final String serviceName) {
        final Retry retry = buildRetry(serviceName);
        final CircuitBreaker circuitBreaker = buildCircuitBreaker(serviceName);
        final Supplier<T> decoratedSupplier = Decorators
                .ofSupplier(supplier)
                .withRetry(retry)
                .withCircuitBreaker(circuitBreaker)
                .withFallback(fallBack)
                .decorate();
        return decoratedSupplier.get();
    }


    private Retry buildRetry(final String serviceName) {
        final Optional<Retry> optionalRetry = retryRegistry
                .getAllRetries()
                .find(r -> r.getName().equals(serviceName))
                .toJavaOptional();
        final boolean exist = optionalRetry.isPresent();
        if (exist) {
            return optionalRetry.get();
        }
        final Retry retry = retryRegistry.retry(serviceName);
        if (restWrapperConfig.isLogEvents()) {
            Retry.EventPublisher publisher = retry.getEventPublisher();
            publisher.onRetry(event -> log.info(event.toString()));
            publisher.onError(event -> log.error(event.toString()));
        }
        return  retry;
    }

    private CircuitBreaker buildCircuitBreaker(final String serviceName) {
        final Optional<CircuitBreaker> optionalCircuitBreaker =  circuitBreakerRegistry
                .getAllCircuitBreakers()
                .find(cb -> cb.getName().equals(serviceName))
                .toJavaOptional();
        final boolean exist = optionalCircuitBreaker.isPresent();

        if (exist) {
            return optionalCircuitBreaker.get();
        }

        final CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);

        if (restWrapperConfig.isLogEvents()) {
            CircuitBreaker.EventPublisher publisher = circuitBreaker.getEventPublisher();
            publisher.onStateTransition((event)-> log.info(event.toString()));
            publisher.onCallNotPermitted((event)-> log.info(event.toString()));
            publisher.onError(event -> log.error(event.toString()));
        }
        return circuitBreaker;
    }
}
