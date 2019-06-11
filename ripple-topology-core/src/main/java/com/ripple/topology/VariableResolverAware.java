package com.ripple.topology;

/**
 * @author jfulton
 */
public interface VariableResolverAware extends Element {

    void resolveVariables(final Topology topology, final VariableResolver resolver);
}
