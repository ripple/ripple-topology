package com.ripple.topology.variables;

import com.ripple.topology.VariableResolver;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * @author jfulton
 */
public class VelocityVariableResolver implements VariableResolver {

    private static final String LOG_TAG = "variables";
    private final VelocityContext context;

    public VelocityVariableResolver() {
        this (new VelocityContext());
    }

    public VelocityVariableResolver(final VelocityContext context) {
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public String resolve(final String template) {
        if (template == null) {
            return null;
        } else {
            StringWriter writer = new StringWriter();
            Velocity.evaluate(context, writer, LOG_TAG, template);
            return writer.toString();
        }
    }

    public void put(String key, Object object) {
        if (object instanceof String) {
            context.put(key, resolve(resolve(object.toString())));
        } else {
            context.put(key, object);
        }
    }

    @Override
    public void putAll(final Map<String, Object> variables) {
        for (Entry<String, Object> pair : variables.entrySet()) {
            put(pair.getKey(), pair.getValue());
        }
    }

    @Override
    public VariableResolver clone() {
        return new VelocityVariableResolver((VelocityContext) context.clone());
    }
}
