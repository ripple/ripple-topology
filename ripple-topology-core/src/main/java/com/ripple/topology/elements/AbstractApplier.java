package com.ripple.topology.elements;

import com.ripple.topology.Resource;
import com.ripple.topology.ScopedVariableSource;
import com.ripple.topology.Topology;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an element that applies something to resources based on Resource key criteria.
 * <p>
 * Resources can be matches based on equalsTo, startsWith, contains, and endsWith criteria.  Any of these criteria that
 * are set act as 'AND' criteria.  Null criteria are not considered.
 * <p>
 * If no criteria is provided, the Applier with apply to all Elements of the Generic Type argument.
 *
 * @author jfulton
 */
@SuppressWarnings("unchecked")
public abstract class AbstractApplier<T extends AbstractApplier<T, R>, R extends Resource> implements Configurer, ScopedVariableSource<T> {

    private Map<String, Object> variables = new LinkedHashMap<>();

    private Class<R> type;
    private String keyEqualsCriteria;
    private String keyStartsCriteria;
    private String keyContainsCriteria;
    private String keyEndsCriteria;
    private Class<?> elementType;

    public AbstractApplier(Class<R> type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public void configure(final Topology topology) {
        for (R resource : topology.getElements(type)) {
            if (shouldApplyTo(resource)) {
                applyTo(resource);
            }
        }
    }

    public abstract void applyTo(R resource);

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getApplyToKeyEqualing() {
        return keyEqualsCriteria;
    }

    public T setApplyToKeyEqualing(final String keyEqualsCriteria) {
        this.keyEqualsCriteria = Objects.requireNonNull(keyEqualsCriteria);
        return (T) this;
    }

    public String getApplyToKeysStartingWith() {
        return keyStartsCriteria;
    }

    public T setApplyToKeysStartingWith(final String keyStartsWith) {
        this.keyStartsCriteria = Objects.requireNonNull(keyStartsWith);
        return (T) this;
    }

    public String getApplyToKeysContaining() {
        return keyContainsCriteria;
    }

    public T setApplyToKeysContaining(final String keyContainsCriteria) {
        this.keyContainsCriteria = Objects.requireNonNull(keyContainsCriteria);
        return (T) this;
    }

    public String getApplyToKeysEndingWith() {
        return keyEndsCriteria;
    }

    public T setApplyToKeysEndingWith(final String keyEndsCriteria) {
        this.keyEndsCriteria = Objects.requireNonNull(keyEndsCriteria);
        return (T) this;
    }

    public Class<?> getApplyToResourcesOfType() {
        return elementType;
    }

    public T setApplyToResourcesOfType(final Class<?> elementType) {
        this.elementType = Objects.requireNonNull(elementType);
        return (T) this;
    }

    protected boolean shouldApplyTo(Resource resource) {
        return
            compliesWithKeyEquality(resource) &&
                compliesWithKeyStartsWith(resource) &&
                compliesWithKeyContains(resource) &&
                compliesWithKeyEndsWith(resource) &&
                compliesWithTypeCriteria(resource) &&
                isNotSelf(resource)
            ;
    }

    private boolean compliesWithKeyEquality(Resource resource) {
        return keyEqualsCriteria == null || resource.getKey().equals(keyEqualsCriteria);
    }

    private boolean compliesWithKeyStartsWith(Resource resource) {
        return keyStartsCriteria == null || resource.getKey().startsWith(keyStartsCriteria);
    }

    private boolean compliesWithKeyContains(Resource resource) {
        return keyContainsCriteria == null || resource.getKey().contains(keyContainsCriteria);
    }

    private boolean compliesWithKeyEndsWith(Resource resource) {
        return keyEndsCriteria == null || resource.getKey().endsWith(keyEndsCriteria);
    }

    private boolean compliesWithTypeCriteria(Resource resource) {
        return elementType == null || elementType.isAssignableFrom(resource.getClass());
    }

    private boolean isNotSelf(Resource resource) {
        return !resource.equals(this);
    }
}
