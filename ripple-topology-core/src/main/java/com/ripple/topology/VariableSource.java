package com.ripple.topology;

import java.util.Map;

/**
 * An {@link Element} that makes it's variables available to all other Elements.
 * <p>
 * VariableSources that use {@link Lifecycle#start(Topology)} to load their variables should be placed above any other
 * Elements relying on the variables loaded in this Element.
 * <p>
 * Any other Variable source containing one or more of the same keys will override variables defined in a global VariableSource.
 *
 * @author jfulton
 */
@SuppressWarnings("unchecked")
public interface VariableSource<T> extends Element {

    Map<String, Object> getVariables();

    default T addVariable(String key, Object value) {
        getVariables().put(key, value);
        return (T) this;
    }
}
