package com.ripple.topology.elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author jfulton
 */
public abstract class AbstractPropertiesAwareResource<T extends AbstractPropertiesAwareResource<T>> extends AbstractResource implements PropertiesAwareResource<T>,
    ScopedVariableSourceResource<T> {

    private Properties properties = new Properties();
    private Map<String, Object> variables = new LinkedHashMap<>();

    public AbstractPropertiesAwareResource(final String key) {
        super(key);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }
}
