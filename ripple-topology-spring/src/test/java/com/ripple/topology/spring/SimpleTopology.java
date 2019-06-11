package com.ripple.topology.spring;

import com.google.common.net.HostAndPort;
import com.ripple.topology.Topology;
import com.ripple.topology.TopologyFactory;
import com.ripple.topology.elements.StaticHostAndPort;
import com.ripple.topology.elements.StaticHttpUrl;
import okhttp3.HttpUrl;

/**
 * @author jfulton
 */
public class SimpleTopology implements TopologyFactory {

    @Override
    public Topology create() {
        Topology topology = new Topology();
        topology.addElement(new StaticHttpUrl("sf", HttpUrl.parse("http://localhost")));
        topology.addElement(new StaticHostAndPort("db", HostAndPort.fromString("localhost:9000")));
        return topology;
    }
}
