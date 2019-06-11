package com.ripple.topology.elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author jfulton
 */
@SuppressWarnings("unchecked")
public abstract class AbstractJvmResource<T> extends AbstractResource implements JvmResource<T>, ScopedVariableSourceResource<T> {

    private Properties properties = new Properties();
    private Map<String, Object> variables = new LinkedHashMap<>();
    private int javaXmx;
    private int javaXms;

    public AbstractJvmResource(final String key, int defaultJavaXmx, int defaultJavaXms) {
        super(key);
        this.javaXmx = defaultJavaXmx;
        this.javaXms = defaultJavaXms;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public int getXmx() {
        return javaXmx;
    }

    @Override
    public T setXmx(final int javaXmx) {
        this.javaXmx = javaXmx;
        return (T) this;
    }

    @Override
    public int getXms() {
        return javaXms;
    }

    @Override
    public T setXms(final int javaXms) {
        this.javaXms = javaXms;
        return (T) this;
    }
}
