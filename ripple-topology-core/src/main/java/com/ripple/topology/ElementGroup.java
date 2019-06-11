package com.ripple.topology;

import com.ripple.topology.utils.ObservableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author jfulton
 */
public interface ElementGroup<T extends ElementGroup> extends Element {

    ObservableList<Element> getElements();

    default ElementGroup addElement(Element element) {
        getElements().add(element);
        return this;
    }

    default T onAddElement(Consumer<Element> listener) {
        getElements().onAdd(listener);
        for (Element element : getElements()) {
            if (element instanceof ElementGroup) {
                ElementGroup group = (ElementGroup) element;
                group.onAddElement(listener);
            }
        }
        return (T) this;
    }

    default T onRemoveElement(Consumer<Element> listener) {
        getElements().onRemove(listener);
        for (Element element : getElements()) {
            if (element instanceof ElementGroup) {
                ElementGroup group = (ElementGroup) element;
                group.onRemoveElement(listener);
            }
        }
        return (T) this;
    }

    default <E extends Element> List<E> getElements(Class<E> type) {
        return getElements(type, t -> true);
    }

    default  <E extends Element> List<E> getElements(Class<E> type, Predicate<E> predicate) {
        List<E> results = new ArrayList<>();
        collect(getElements(), type, results, Objects.requireNonNull(predicate, "'predicate' may not be null"));
        return results;
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    default <E extends Element> void collect(List<Element> items, Class<E> type, List<E> results, Predicate<E> predicate) {
        for (Element element : items) {
            if (type.isAssignableFrom(element.getClass()) && predicate.test((E) element)) {
                results.add((E) element);
            }
            if (element instanceof ElementGroup) {
                collect(((ElementGroup)element).getElements(), type, results, predicate);
            }
        }
    }
}
