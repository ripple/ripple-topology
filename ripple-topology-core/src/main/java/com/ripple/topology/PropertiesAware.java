package com.ripple.topology;

import java.util.Properties;

/**
 * @author jfulton
 */
@SuppressWarnings("unchecked")
public interface PropertiesAware<T> extends Element {

    Properties getProperties();

    default T addProperty(String key, String value) {
        getProperties().put(key, value);
        return (T) this;
    }
}
