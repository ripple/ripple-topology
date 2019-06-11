package com.ripple.topology;

import java.util.Map;

/**
 * @author jfulton
 */
public interface VariableResolver extends Element {

    String resolve(String template);

    void put(String key, Object value);

    void putAll(Map<String, Object> variables);

    VariableResolver clone();
}
