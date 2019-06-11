package com.ripple.topology.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class HealthUtilsTest {

    @Test(timeOut = 5000)
    public void testAlreadyHealthyReturnsImmediately() {
        HealthUtils.waitForHealth(Duration.ofSeconds(30), Duration.ofSeconds(15), () -> true);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "maxWait must be larger than retryPause")
    public void testRetryPauseShouldNotBeGreaterThanMaxWait() {
        HealthUtils.waitForHealth(Duration.ofMillis(50), Duration.ofMillis(100), () -> true);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "maxWait must be larger than retryPause")
    public void testRetryPauseShouldNotBeEqualToMaxWait() {
        HealthUtils.waitForHealth(Duration.ofMillis(50), Duration.ofMillis(50), () -> true);
    }

    @Test
    public void testMaxWaitCanBeGreaterThanRetryPause() {
        HealthUtils.waitForHealth(Duration.ofMillis(51), Duration.ofMillis(50), () -> true);
    }

    @Test
    public void testHealthPassesWithinMaxTime() {
        AtomicInteger counter = new AtomicInteger(1);

        boolean isHealthy = HealthUtils
            .waitForHealth(Duration.ofSeconds(3), Duration.ofSeconds(1), () -> counter.getAndIncrement() >= 3);
        assertThat(isHealthy, is(true));
    }

    @Test
    public void testHealthPassesWithinMaxTimeWithLastChance() {
        AtomicInteger counter = new AtomicInteger(1);

        boolean isHealthy = HealthUtils
            .waitForHealth(Duration.ofSeconds(3), Duration.ofSeconds(1), () -> counter.getAndIncrement() >= 4);
        assertThat(isHealthy, is(true));
    }

    @Test
    public void testHealthFailsOutsideMaxTime() {
        AtomicInteger counter = new AtomicInteger(1);

        boolean isHealthy = HealthUtils
            .waitForHealth(Duration.ofSeconds(3), Duration.ofSeconds(1), () -> counter.getAndIncrement() >= 5);
        assertThat(isHealthy, is(false));
    }
}
