package com.ripple.topology;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author jfulton
 */
public interface HealthCheck extends Element {

    @JsonIgnore
    boolean isHealthy();
}
