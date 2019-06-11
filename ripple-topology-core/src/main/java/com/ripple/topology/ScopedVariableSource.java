package com.ripple.topology;

/**
 * A VariableSource that only provides variables for itself, and potentially it's children.
 *
 * @author jfulton
 */
public interface ScopedVariableSource<T> extends VariableSource<T> {

}
