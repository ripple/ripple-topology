package com.ripple.topology.elements;

import com.ripple.topology.Lifecycle;
import com.ripple.topology.Topology;
import java.util.concurrent.CompletableFuture;

/**
 * @author jfulton
 */
public interface Configurer extends Lifecycle {

    void configure(Topology topology);

    @Override
    default CompletableFuture<Void> start(final Topology topology) {
        return CompletableFuture.runAsync(() -> configure(topology));
    }

    @Override
    default CompletableFuture<Void> stop(final Topology topology) {
        return CompletableFuture.completedFuture(null);
    }
}
