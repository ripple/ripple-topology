package com.ripple.topology;

import com.ripple.topology.elements.AbstractPropertiesAwareResource;

/**
 * @author jfulton
 */
public class XCurrentResource extends AbstractPropertiesAwareResource {

    public XCurrentResource(final String key) {
        super(key);
    }

    public XCurrentResource addProperty(String key, String value) {
        getProperties().put(key, value);
        return this;
    }
}
