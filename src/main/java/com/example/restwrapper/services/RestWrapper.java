package com.example.restwrapper.services;

import java.util.function.Function;
import java.util.function.Supplier;

public interface RestWrapper {

    <T> T executeRestWithRetry(Supplier<T> supplier, String serviceName);

    <T> T executeRestWithCircuitBreaker(Supplier<T> supplier, String serviceName);

    <T> T executeRestWithCircuitBreaker(Supplier<T> supplier, Function<Throwable, T> fallBack, String serviceName);

    <T> T executeRestWithCircuitBreakerAndRetry(Supplier<T> supplier, String serviceName);

    <T> T executeRestWithCircuitBreakerAndRetry(Supplier<T> supplier, Function<Throwable, T> fallBack, String serviceName);
}
