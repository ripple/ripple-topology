package com.ripple.topology.elements;

import com.ripple.topology.Resource;

/**
 * A {@link Resource} that represents a host on a network.
 *
 * @author jfulton
 */
public interface HostResource extends Resource {

    /**
     * The Resource's Host String as either a DNS host name or IP Address.
     *
     * @return Resource Host name
     */
    String getHost();
}
