package com.example.restwrapper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("rest-wrapper")
public class RestWrapperConfig {
    private int retryAttempts;
    private long delayBeforeRetry;
    private double retryMultiplier;
    private int minimumNumberOfCalls;
    private float failureRateThreshold;
    private float slowCallRateThreshold;
    private long slowCallDurationThreshold;
    private long waitDurationInOpenState;
    private int permittedNumberOfCallsInHalfOpenState;
    private long maxWaitDurationInHalfOpenState;
    private String slidingWindowType;
    private int slidingWindowSize;
    private boolean writableStackTraceEnabled = true;
    private boolean automaticTransitionFromOpenToHalfOpenEnabled = false;
    private boolean logEvents = false;
}
