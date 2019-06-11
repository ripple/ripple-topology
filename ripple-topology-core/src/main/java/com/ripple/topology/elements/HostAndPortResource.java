package com.ripple.topology.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.net.HostAndPort;
import com.ripple.topology.Resource;

/**
 * A {@link Resource} representing a TCP application on a network.  A Resource hosting one or more application would
 * typically implements this to expose it's Primary Application endpoint.  As an example, if a database is hosted
 * on a server that also allows SSH access for management, the Resource reprenting the host would implement this interface
 * to expose the database's endpoint, and the SSH endpoint would be exposed as either a separate Resource, or with a
 * different interface.
 *
 * @author jfulton
 */
public interface HostAndPortResource extends Resource, HostResource {

    /**
     * The Resource's {@link HostAndPort}
     *
     * @return an application endpoint
     */
    HostAndPort getHostAndPort();

    @JsonIgnore
    default String getHost() {
        return getHostAndPort() != null ? getHostAndPort().getHost() : null;
    }
}
