package com.ripple.topology.utils;

import com.ripple.topology.Resource;
import com.ripple.topology.Topology;
import com.ripple.topology.elements.HostAndPortResource;
import com.ripple.topology.elements.HttpUrlResource;
import com.ripple.topology.elements.StaticHostAndPort;
import com.ripple.topology.elements.StaticHttpUrl;
import com.ripple.topology.elements.StaticHttpUrlAndHostAndPort;
import java.util.Objects;

/**
 * @author jfulton
 */
public class TopologyUtils {

    public static Topology createProxy(final Topology source) {
        Objects.requireNonNull(source);
        Topology results = new Topology();
        for (Resource resource : source.getElements(Resource.class,
            resource -> resource instanceof HttpUrlResource || resource instanceof HostAndPortResource)) {
            if (resource instanceof  HttpUrlResource && resource instanceof HostAndPortResource) {
                StaticHttpUrlAndHostAndPort castResource = (StaticHttpUrlAndHostAndPort) resource;
                results.addElement(new StaticHttpUrlAndHostAndPort(castResource.getKey(), castResource.getHttpUrl(), castResource.getHostAndPort()));
            } else if (resource instanceof HttpUrlResource) {
                results.addElement(new StaticHttpUrl(resource.getKey(), ((HttpUrlResource)resource).getHttpUrl()));
            } else {
                results.addElement(new StaticHostAndPort(resource.getKey(), ((HostAndPortResource)resource).getHostAndPort()));
            }
        }
        return results;
    }
}
