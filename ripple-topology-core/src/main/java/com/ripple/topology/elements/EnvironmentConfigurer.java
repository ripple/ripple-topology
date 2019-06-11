package com.ripple.topology.elements;

import com.ripple.topology.EnvironmentAware;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author matt
 */
public class EnvironmentConfigurer extends AbstractApplier<EnvironmentConfigurer, EnvironmentAwareResource> implements EnvironmentAware {

    private Map<String, String> environment = new LinkedHashMap<>();

    public EnvironmentConfigurer() {
        super(EnvironmentAwareResource.class);
    }

    public EnvironmentConfigurer(final String keyEqualsCriteria) {
        this();
        this.setApplyToKeyEqualing(keyEqualsCriteria);
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public EnvironmentConfigurer addEnvironmentVariable(String key, String value) {
        getEnvironment().put(key, value);
        return this;
    }

    @Override
    public void applyTo(final EnvironmentAwareResource resource) {
        for (String key : environment.keySet()) {
            if (!resource.getEnvironment().containsKey(key)) {
                resource.getEnvironment().put(key, environment.get(key));
            }
        }
    }
}
