package com.ripple.topology.variables;

import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfulton
 */
public class VariableUtils {

    private static final Logger logger = LoggerFactory.getLogger(VariableUtils.class);

    /**
     * Overrides any value who's key is present as a System Property
     *
     * @param variables map containing variables to replace
     * @param sourceType a descriptive name for logging purposes
     */
    public static void overrideFromSystemProperties(final Map<String, Object> variables, final String sourceType) {
        Objects.requireNonNull(variables);
        for (String key : variables.keySet()) {
            if (System.getProperties().containsKey(key)) {
                logger.info("Replacing {} '{}' from System Property", sourceType, key);
                variables.put(key, System.getProperty(key));
            }
        }
    }
}
