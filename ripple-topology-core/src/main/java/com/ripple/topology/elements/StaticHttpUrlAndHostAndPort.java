package com.ripple.topology.elements;

import com.google.common.net.HostAndPort;
import java.util.Objects;
import okhttp3.HttpUrl;

/**
 * @author jfulton
 */
public class StaticHttpUrlAndHostAndPort extends AbstractResource implements HttpUrlResource, HostAndPortResource {

    private HttpUrl httpUrl;
    private HostAndPort hostAndPort;

    public StaticHttpUrlAndHostAndPort() {
        super("");
    }

    public StaticHttpUrlAndHostAndPort(final String key, HttpUrl httpUrl, HostAndPort hostAndPort) {
        super(key);
        this.httpUrl = httpUrl;
        this.hostAndPort = hostAndPort;
    }

    @Override
    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public StaticHttpUrlAndHostAndPort setHostAndPort(final HostAndPort hostAndPort) {
        this.hostAndPort = Objects.requireNonNull(hostAndPort);
        return this;
    }

    @Override
    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    public StaticHttpUrlAndHostAndPort setHttpUrl(final HttpUrl httpUrl) {
        this.httpUrl = Objects.requireNonNull(httpUrl);
        return this;
    }
}
