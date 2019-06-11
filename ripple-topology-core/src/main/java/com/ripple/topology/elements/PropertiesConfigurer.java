package com.ripple.topology.elements;

import com.ripple.topology.PropertiesAware;
import java.util.Properties;

/**
 * @author jfulton
 */
public class PropertiesConfigurer extends AbstractApplier<PropertiesConfigurer, PropertiesAwareResource> implements PropertiesAware {

    private Properties properties = new Properties();

    public PropertiesConfigurer() {
        super(PropertiesAwareResource.class);
    }

    public PropertiesConfigurer(final String keyEqualsCriteria) {
        this();
        setApplyToKeyEqualing(keyEqualsCriteria);
    }

    @Override
    public void applyTo(final PropertiesAwareResource resource) {
        for (String key : properties.stringPropertyNames()) {
            if (!resource.getProperties().containsKey(key)) {
                resource.getProperties().put(key, properties.getProperty(key));
            }
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public PropertiesConfigurer addProperty(String key, String value) {
        getProperties().put(key, value);
        return this;
    }
}

