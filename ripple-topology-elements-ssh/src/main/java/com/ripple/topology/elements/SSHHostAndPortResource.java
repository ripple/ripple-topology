package com.ripple.topology.elements;

import com.google.common.net.HostAndPort;
import com.ripple.topology.Resource;

/**
 * @author jfulton
 */
public interface SSHHostAndPortResource extends Resource {

    HostAndPort getSSHHostAndPort();
}
