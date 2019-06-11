package com.ripple.topology.elements;

import com.ripple.topology.Topology;
import com.ripple.topology.VariableResolver;
import com.ripple.topology.VariableResolverAware;
import java.util.Objects;
import okhttp3.HttpUrl;

/**
 * @author jfulton
 */
public class StaticHttpUrl extends AbstractResource implements HttpUrlResource, VariableResolverAware {

    private HttpUrl httpUrl;

    public StaticHttpUrl() {
        super("");
    }

    public StaticHttpUrl(final String key, final HttpUrl httpUrl) {
        super(key);
        this.httpUrl = httpUrl;
    }

    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(final HttpUrl httpUrl) {
        this.httpUrl = Objects.requireNonNull(httpUrl);
    }

    @Override
    public void resolveVariables(final Topology topology, final VariableResolver resolver) {
        this.httpUrl = HttpUrl.parse(resolver.resolve(httpUrl.toString()));
    }
}
