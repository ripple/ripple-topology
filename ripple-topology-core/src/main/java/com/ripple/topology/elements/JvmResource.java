package com.ripple.topology.elements;

import com.ripple.topology.PropertiesAware;
import com.ripple.topology.Resource;

/**
 * @author jfulton
 */
public interface JvmResource<T> extends Resource, PropertiesAware<T> {

    int getXmx();

    T setXmx(int xmx);

    int getXms();

    T setXms(int xms);
}
