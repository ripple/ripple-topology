package com.ripple.topology;

import java.util.concurrent.CompletableFuture;

/**
 * @author jfulton
 */
public interface Lifecycle extends Element {
    
    CompletableFuture<Void> start(Topology topology);

    CompletableFuture<Void> stop(Topology topology);
}
