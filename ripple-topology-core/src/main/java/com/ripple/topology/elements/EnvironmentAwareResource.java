package com.ripple.topology.elements;

import com.ripple.topology.EnvironmentAware;
import com.ripple.topology.Resource;

/**
 * @author matt
 */
public interface EnvironmentAwareResource<T extends EnvironmentAwareResource<T>> extends Resource, EnvironmentAware<T> {

}
