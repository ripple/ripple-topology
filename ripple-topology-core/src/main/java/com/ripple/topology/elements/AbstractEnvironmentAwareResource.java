package com.ripple.topology.elements;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jfulton
 */
public class AbstractEnvironmentAwareResource extends AbstractResource implements EnvironmentAwareResource,
    ScopedVariableSourceResource {

    private final Map<String, String> environment = new LinkedHashMap<>();
    private final Map<String, Object> variables = new LinkedHashMap<>();

    public AbstractEnvironmentAwareResource(final String key) {
        super(key);
    }

    @Override
    public Map<String, String> getEnvironment() {
        return environment;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }
}
