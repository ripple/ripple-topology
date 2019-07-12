package com.ripple.topology.spring.util;

import com.ripple.topology.Topology;

public class TopologySynchronousLifecycleManager {

    private Topology topology;

    public TopologySynchronousLifecycleManager(final Topology topology) {
        this.topology = topology;
    }

    public void start() {
        topology.start().join();
    }

    public void stop() {
        topology.stop().join();
    }
}
