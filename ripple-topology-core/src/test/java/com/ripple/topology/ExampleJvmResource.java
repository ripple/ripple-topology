package com.ripple.topology;

import com.ripple.topology.elements.AbstractJvmResource;

/**
 * @author jfulton
 */
public class ExampleJvmResource extends AbstractJvmResource<ExampleJvmResource> {

    public ExampleJvmResource(final String key) {
        super(key, 512, 256);
    }
}
