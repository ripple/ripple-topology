package com.ripple.topology.utils;

import com.ripple.topology.HealthCheck;
import com.ripple.topology.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfulton
 */
public class HealthUtils {

    private static final Logger logger = LoggerFactory.getLogger(HealthUtils.class);

    public static boolean waitForHealth(Duration maxWait, Duration retryPause, HealthCheck healthCheck) {
        String id = healthCheck instanceof Resource ? ((Resource) healthCheck).getKey()
            : healthCheck.getClass().getName();
        return waitForHealth(maxWait, retryPause, id, healthCheck);
    }

    public static boolean waitForHealth(Duration maxWait, Duration retryPause, String healthSubject, HealthCheck healthCheck) {
        Objects.requireNonNull(maxWait);
        Objects.requireNonNull(retryPause);
        Objects.requireNonNull(healthSubject);
        Objects.requireNonNull(healthCheck);
        if (maxWait.compareTo(retryPause) <= 0) {
            throw new RuntimeException("maxWait must be larger than retryPause");
        }
        final long startTime = System.currentTimeMillis();
        final long timeout = startTime + maxWait.toMillis();
        while (System.currentTimeMillis() < timeout) {
            if (healthCheck.isHealthy()) {
                logger.info("{} available within {} millis", healthSubject, System.currentTimeMillis() - startTime);
                return true;
            } else {
                logger.info("Waiting a maximum of {} millis for {} availability", maxWait.toMillis() - (System.currentTimeMillis() - startTime), healthSubject);
                try {
                    Thread.sleep(retryPause.toMillis());
                } catch (InterruptedException ex) {
                    logger.error("Interrupted", ex);               
                }
            }
        }
        return healthCheck.isHealthy();
    }

    public static boolean waitForHealth(Duration maxWait, Duration retryPause, List<HealthCheck> healthChecks) {
        if (healthChecks.size() > 0) {
            Executor executor = Executors.newFixedThreadPool(healthChecks.size());
            CountDownLatch latch = new CountDownLatch(healthChecks.size());
            final AtomicInteger healthCheckSuccesses = new AtomicInteger();

            for (HealthCheck healthCheck : healthChecks) {
                executor.execute(() -> {
                    if (waitForHealth(maxWait, retryPause, healthCheck)) {
                        healthCheckSuccesses.incrementAndGet();
                    }
                    latch.countDown();
                });
            }

            try {
                latch.await(maxWait.toMillis() + 1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Error awaiting latch", e);
            } finally {
                ((ExecutorService) executor).shutdown();
            }
            if (healthCheckSuccesses.get() < healthChecks.size()) {
                logger.warn("{} out of {} health checks failed to be healthy within {} millis",
                    (healthChecks.size() - healthCheckSuccesses.get()), healthChecks.size(),
                    maxWait);
            }
            return healthCheckSuccesses.get() == healthChecks.size();
        }
        return true;
    }
}
