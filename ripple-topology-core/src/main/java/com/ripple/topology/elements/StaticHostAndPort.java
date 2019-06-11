package com.ripple.topology.elements;

import com.google.common.net.HostAndPort;
import com.ripple.topology.Topology;
import com.ripple.topology.VariableResolver;
import com.ripple.topology.VariableResolverAware;
import java.util.Objects;

/**
 * @author jfulton
 */
public class StaticHostAndPort extends AbstractResource implements HostAndPortResource, VariableResolverAware {

    private HostAndPort endpoint;

    public StaticHostAndPort() {
        super("");
    }

    public StaticHostAndPort(final String key, final HostAndPort endpoint) {
        super(key);
        this.endpoint = endpoint;
    }

    @Override
    public HostAndPort getHostAndPort() {
        return endpoint;
    }

    public void setHostAndPort(final String endpoint) {
        this.endpoint = HostAndPort.fromString(Objects.requireNonNull(endpoint));
    }

    @Override
    public void resolveVariables(final Topology topology, final VariableResolver resolver) {
        this.endpoint = HostAndPort.fromString(resolver.resolve(endpoint.toString()));
    }
}
