package com.ripple.topology.elements;

import com.ripple.topology.Element;
import com.ripple.topology.ElementGroup;
import com.ripple.topology.utils.ObservableList;

/**
 * @author jfulton
 */
public abstract class AbstractElementGroup<T extends ElementGroup<T>> implements ElementGroup<T> {

    private ObservableList<Element> elements = new ObservableList<>();

    @Override
    public ObservableList<Element> getElements() {
        return elements;
    }
}
