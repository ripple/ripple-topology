package com.ripple.topology.elements;

/**
 * @author jfulton
 */
public class VariablesConfigurer extends AbstractApplier<VariablesConfigurer, ScopedVariableSourceResource> {

    public VariablesConfigurer() {
        super(ScopedVariableSourceResource.class);
    }

    @Override
    public void applyTo(final ScopedVariableSourceResource resource) {
        resource.getVariables().putAll(getVariables());
    }
}
