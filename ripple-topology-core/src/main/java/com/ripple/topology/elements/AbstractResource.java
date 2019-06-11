package com.ripple.topology.elements;

import com.ripple.topology.Resource;
import java.util.Objects;

/**
 * @author jfulton
 */
public abstract class AbstractResource implements Resource {

    private final String key;

    public AbstractResource(final String key) {
        this.key = Objects.requireNonNull(key, "key must not be null");
    }

    @Override
    public String getKey() {
        return key;
    }
}
