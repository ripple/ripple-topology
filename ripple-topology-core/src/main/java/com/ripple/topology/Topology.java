package com.ripple.topology;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Lists;
import com.ripple.topology.io.ContentLoader;
import com.ripple.topology.io.DefaultContentLoader;
import com.ripple.topology.utils.ObservableList;
import com.ripple.topology.variables.VariableUtils;
import com.ripple.topology.variables.VelocityVariableResolver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfulton
 */
@SuppressWarnings("unchecked")
@JsonInclude(Include.NON_DEFAULT)
@JsonPropertyOrder({"variables", "elements"})
public class Topology {

    private static final Logger logger = LoggerFactory.getLogger(Topology.class);
    private final ContentLoader contentLoader = new DefaultContentLoader();
    private final Map<String, Resource> resources = new HashMap<>();
    private final ObservableList<Element> elements;
    private final Map<String, Object> variables = new LinkedHashMap<>();
    private final Consumer<Element> addListener = this::registerResources;
    private final Consumer<Element> removeListener = this::deregisterResources;
    private boolean allowSystemPropertyOverrides = true;
    private AtomicBoolean failedDuringStartup = new AtomicBoolean(false);
    private AtomicBoolean shutDown = new AtomicBoolean(false);

    public Topology() {
        this(new ObservableList<>());
    }


    // Jackson creates the list outside of the Topology, and sets it after the list is already populated, so we need a
    // special constructor that ensures the elements list has appropriate add/remove listeners required to make element
    // adds and removals reflect properly in the resources map.
    @JsonCreator
    protected Topology(@JsonProperty("elements") ObservableList<Element> elements) {
        elements.onAdd(addListener);
        elements.onRemove(removeListener);
        this.elements = elements;
        for (Element element : this.elements) {
            registerResources(element);
        }
    }

    public <T extends Resource> T getResource(ResourceKey key, Class<T> type) {
        return getResourceOptional(Objects.requireNonNull(key, "'key' cannot be null").getKey(), type)
            .orElseThrow(() -> new RuntimeException("Resource by key '" + key + "' not found"));
    }

    public <T extends Resource> Optional<T> getResourceOptional(ResourceKey key, Class<T> type) {
        return getResourceOptional(Objects.requireNonNull(key, "'key' cannot be null").getKey(), type);
    }

    public <T extends Resource> T getResource(String key, Class<T> type) {
        return getResourceOptional(Objects.requireNonNull(key, "'key' cannot be null"), type)
            .orElseThrow(() -> new RuntimeException("Resource by key '" + key + "' not found"));
    }

    public <T extends Resource> Optional<T> getResourceOptional(String key, Class<T> type) {
        Resource resource = resources.get(Objects.requireNonNull(key, "'key' cannot be null"));
        if (resource == null) {
            return Optional.empty();
        } else {
            if (!type.isAssignableFrom(resource.getClass())) {
                throw new IllegalStateException(
                    "Resource by key '" + key + "' is of type '" + resource.getClass() + "', but was requested as '" + type
                        + "'");
            } else {
                //noinspection unchecked
                return Optional.of((T) resource);
            }
        }
    }

    public <T extends Element> List<T> getElements(Class<T> type) {
        return getElements(type, t -> true);
    }

    public <T extends Element> List<T> getElements(Class<T> type, Predicate<T> predicate) {
        List<T> results = new ArrayList<>();
        collect(getElements(), type, results, Objects.requireNonNull(predicate, "'predicate' may not be null"));
        return results;
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    private <T extends Element> void collect(List<Element> items, Class<T> type, List<T> results, Predicate<T> predicate) {
        for (Element element : items) {
            if (type.isAssignableFrom(element.getClass()) && predicate.test((T) element)) {
                results.add((T) element);
            }
            if (element instanceof ElementGroup) {
                collect(((ElementGroup)element).getElements(), type, results, predicate);
            }
        }
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public Topology addVariable(String key, Object value) {
        getVariables().put(key, value);
        return this;
    }

    public ObservableList<Element> getElements() {
        return elements;
    }

    public Topology addElement(Element element) {
        getElements().add(element);
        return this;
    }

    private void registerResources(Element element) {
        if (element instanceof Resource) {
            Resource resource = (Resource) element;
            resources.put(resource.getKey(), resource);
        }
        if (element instanceof ElementGroup) {
            ((ElementGroup)element).onAddElement(addListener);
            ((ElementGroup)element).onRemoveElement(removeListener);
            for (Element u : ((ElementGroup<ElementGroup>) element).getElements()) {
                registerResources(u);
            }
        }
    }

    private void deregisterResources(Element element) {
        if (element instanceof Resource) {
            Resource resource = (Resource) element;
            resources.remove(resource.getKey());
        }
        if (element instanceof ElementGroup) {
            ((ElementGroup)element).getElements().getOnAddListeners().remove(addListener);
            ((ElementGroup)element).getElements().getOnRemoveListeners().remove(removeListener);
            for (Element u : ((ElementGroup<ElementGroup>) element).getElements()) {
                deregisterResources(u);
            }
        }
    }

    public void substituteVariables(Element element) {
        if (!(element instanceof VariableResolverAware || element instanceof PropertiesAware || element instanceof
            EnvironmentAware)) {
            return;
        }

        VariableResolver resolver = new VelocityVariableResolver();

        // Make all of the resources available
        for (Entry<String, Resource> pair : resources.entrySet()) {
            resolver.put(pair.getKey(), pair.getValue());
        }

        // Make any GlobalVariableSources' variables available to all Elements, overridable by everything else
        for (VariableSource source : getElements(VariableSource.class,
            variableSource -> !(variableSource instanceof ScopedVariableSource))) {

            resolver.putAll(source.getVariables());
        }

        // Make all of the Topology variables available
        getVariables().forEach(resolver::put);

        substituteVariables(element, resolver);
    }

    public void substituteVariables(Element element, VariableResolver resolver) {
        // Make the current Element's Resource key available
        if (element instanceof Resource) {
            resolver.put("key", ((Resource) element).getKey());
        }

        // Make any Element's scoped variables available
        if (element instanceof ScopedVariableSource) {
            resolver.putAll(((ScopedVariableSource) element).getVariables());
        }

        // Allow Elements to resolve their own variables
        if (element instanceof VariableResolverAware) {
            ((VariableResolverAware) element).resolveVariables(this, resolver);
        }

        // Resolve variables in any Property keys and values
        if (element instanceof PropertiesAware) {
            Properties originalProperties = new Properties();
            Properties properties = ((PropertiesAware) element).getProperties();
            originalProperties.putAll(properties);
            properties.clear();
            for (String key : originalProperties.stringPropertyNames()) {
                properties.put(resolver.resolve(key), resolver.resolve(originalProperties.getProperty(key)));
            }
        }

        // Resolve variables in any Environment Variable keys and values
        if (element instanceof EnvironmentAware) {
            final Map<String, String> environment = ((EnvironmentAware) element).getEnvironment();
            Map<String, String> originalEnvironment = new HashMap<>(environment);
            environment.clear();

            originalEnvironment.forEach((originalKey, originalValue) -> environment.put(resolver.resolve(originalKey),
                resolver.resolve(originalValue)));
        }
    }

    public ContentLoader contentLoader() {
        return contentLoader;
    }

    public boolean isAllowSystemPropertyOverrides() {
        return allowSystemPropertyOverrides;
    }

    public Topology setAllowSystemPropertyOverrides(final boolean allowSystemPropertyOverrides) {
        this.allowSystemPropertyOverrides = allowSystemPropertyOverrides;
        return this;
    }

    public CompletableFuture<Topology> start() {
        return CompletableFuture.runAsync(() -> {
            if (isAllowSystemPropertyOverrides()) {
                VariableUtils.overrideFromSystemProperties(getVariables(), "global variable");
            }
            for (Element element : elements) {
                substituteVariables(element);
                if (element instanceof Lifecycle) {
                    try {
                        ((Lifecycle) element).start(this).join();
                    } catch (Exception ex) {
                        failedDuringStartup.set(true);
                        logger.error("Error starting topology due to an exception in a Lifecycle: {}", element, ex);
                        break;
                    }
                }
            }
        }).thenApply(aVoid -> this);
    }

    public Topology startSync() {
        return start().join();
    }

    public CompletableFuture<Void> stop() {
        if (!shutDown.getAndSet(true)) {
            final List<Element> reversed = Lists.reverse(elements);
            return CompletableFuture.runAsync(() -> {
                for (Element element : reversed) {
                    if (element instanceof Lifecycle) {
                        try {
                            ((Lifecycle) element).stop(this).join();
                        } catch (Exception ex) {
                            logger.error("Error shutting stopping Element: {}", element, ex);
                        }
                    }
                }
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    public void stopSync() {
        stop().join();
    }

    public Topology registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!shutDown.get()) {
                stopSync();
            }
        }));
        return this;
    }

    public boolean hasFailedDuringStartup() {
        return failedDuringStartup.get();
    }
}
