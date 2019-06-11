package com.ripple.topology.elements;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jfulton
 */
public abstract class AbstractPropertiesAndEnvironmentAwareResource<T
    extends AbstractPropertiesAndEnvironmentAwareResource<T>>
    extends AbstractPropertiesAwareResource<T>
    implements EnvironmentAwareResource<T> {

    private Map<String, String> environment = new LinkedHashMap<>();

    public AbstractPropertiesAndEnvironmentAwareResource(final String key) {
        super(key);
    }

    @Override
    public Map<String, String> getEnvironment() {
        return environment;
    }
}
