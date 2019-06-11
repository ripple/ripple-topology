package com.ripple.topology;

import java.util.Map;

/**
 * @author matt
 * @author jfulton
 */
@SuppressWarnings("unchecked")
public interface EnvironmentAware<T extends EnvironmentAware<T>> extends Element {
    Map<String, String> getEnvironment();

    default T addEnvironmentVariable(String key, String value) {
        getEnvironment().put(key, value);
        return (T) this;
    }
}
