package com.ripple.topology.elements;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.ripple.topology.Element;
import com.ripple.topology.Lifecycle;
import com.ripple.topology.ScopedVariableSource;
import com.ripple.topology.Topology;
import com.ripple.topology.VariableResolver;
import com.ripple.topology.VariableResolverAware;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Warning: Topology is NOT threadsafe.  Care should be taken when running things in parallel.  It is generally safe
 * to start up resources in parallel, and perhaps run parallel configurations against remote resources in parallel, but
 * it is generally not safe to manipulate the Topology instance in any way, asynchronously.
 *
 * @author jfulton
 */
@JsonTypeName("parallel")
public class ParallelElementGroup extends AbstractElementGroup<ParallelElementGroup> implements Lifecycle, ScopedVariableSource<ParallelElementGroup>,
    VariableResolverAware {

    private final Map<String, Object> variables = new LinkedHashMap<>();
    private VariableResolver variableResolver;

    @Override
    public CompletableFuture<Void> start(final Topology topology) {
        List<CompletableFuture> futures = new ArrayList<>();
        // Only substitute variables based on resources outside this parallel group, making substitution deterministic.
        // We don't want resources within this group depending on each other, with random results.
        for (Element element : getElements()) {
            topology.substituteVariables(element, variableResolver.clone());
        }
        for (Element element : getElements()) {
            if (element instanceof Lifecycle) {
                futures.add(((Lifecycle) element).start(topology));
            }
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
    }

    @Override
    public CompletableFuture<Void> stop(final Topology topology) {
        List<CompletableFuture> futures = new ArrayList<>();
        for (Element element : getElements()) {
            if (element instanceof Lifecycle) {
                futures.add(((Lifecycle) element).stop(topology));
            }
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
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
