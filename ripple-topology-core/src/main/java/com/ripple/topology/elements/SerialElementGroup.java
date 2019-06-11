package com.ripple.topology.elements;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ripple.topology.Element;
import com.ripple.topology.Lifecycle;
import com.ripple.topology.ScopedVariableSource;
import com.ripple.topology.Topology;
import com.ripple.topology.VariableResolver;
import com.ripple.topology.VariableResolverAware;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author jfulton
 */
@JsonTypeName("serial")
public class SerialElementGroup extends AbstractElementGroup<SerialElementGroup> implements Lifecycle, ScopedVariableSource<SerialElementGroup>, VariableResolverAware {

    private final Map<String, Object> variables = new LinkedHashMap<>();
    private VariableResolver variableResolver;

    @Override
    public CompletableFuture<Void> start(final Topology topology) {
        for (Element element : getElements()) {
            assert variableResolver != null;
            topology.substituteVariables(element, variableResolver.clone());
            if (element instanceof Lifecycle) {
                ((Lifecycle) element).start(topology).join();
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stop(final Topology topology) {
        for (Element element : getElements()) {
            if (element instanceof Lifecycle) {
                ((Lifecycle) element).stop(topology).join();
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public void resolveVariables(final Topology topology, final VariableResolver resolver) {
        this.variableResolver = resolver;
    }
}
