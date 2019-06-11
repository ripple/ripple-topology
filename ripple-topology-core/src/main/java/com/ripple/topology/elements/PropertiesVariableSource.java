package com.ripple.topology.elements;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ripple.topology.Lifecycle;
import com.ripple.topology.Topology;
import com.ripple.topology.VariableSource;
import com.ripple.topology.variables.VariableUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link VariableSource} that can optionally load variables from a classpath file.
 * <p>
 * This Element must be placed above any other Element's relying on the variables loaded in this Element.
 * <p>
 * Any other Variable source containing one or more of the same keys will override variables defined
 *
 * @author jfulton
 */
@JsonInclude(Include.NON_DEFAULT)
@JsonPropertyOrder(alphabetic = true)
public class PropertiesVariableSource implements VariableSource<PropertiesVariableSource>, Lifecycle {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesVariableSource.class);
    private String propertiesPath;
    private final Map<String, Object> variables = new LinkedHashMap<>();
    private boolean allowSystemPropertyOverrides = true;

    public PropertiesVariableSource() {
        // Jackson Only
    }

    public PropertiesVariableSource(final String propertiesPath) {
        this.propertiesPath = Objects.requireNonNull(propertiesPath);
    }

    public String getPropertiesPath() {
        return propertiesPath;
    }

    public PropertiesVariableSource setPropertiesPath(final String propertiesPath) {
        this.propertiesPath = Objects.requireNonNull(propertiesPath);
        return this;
    }

    @Override
    public CompletableFuture<Void> start(final Topology topology) {
        if (propertiesPath != null) {
            logger.info("Loading properties from {}", propertiesPath);

            final Properties properties = new Properties();
            try (final InputStream stream = this.getClass().getResourceAsStream(propertiesPath)) {
                properties.load(stream);
                for (String key : properties.stringPropertyNames()) {
                    getVariables().put(key, properties.getProperty(key));
                }
                if (isAllowSystemPropertyOverrides()) {
                    VariableUtils.overrideFromSystemProperties(variables, getClass().getSimpleName());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error loading properties from " + propertiesPath, e);
            }
        } else {
            logger.warn("propertyPath is null");
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stop(final Topology topology) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public PropertiesVariableSource addVariable(final String key, final Object value) {
        this.variables.put(key, value);
        return this;
    }

    public boolean isAllowSystemPropertyOverrides() {
        return allowSystemPropertyOverrides;
    }

    public PropertiesVariableSource setAllowSystemPropertyOverrides(final boolean allowSystemPropertyOverrides) {
        this.allowSystemPropertyOverrides = Objects.requireNonNull(allowSystemPropertyOverrides);
        return this;
    }
}
