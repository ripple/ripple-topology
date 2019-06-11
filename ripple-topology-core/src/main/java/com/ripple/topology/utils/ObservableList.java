package com.ripple.topology.utils;

import com.google.common.collect.ForwardingList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author jfulton
 */
@SuppressWarnings({"NullableProblems", "unchecked"})
public class ObservableList<T> extends ForwardingList<T> {

    private List<T> delegate;
    private ArrayList<Consumer<T>> onAddListeners = new ArrayList<>();
    private ArrayList<Consumer<T>> onRemoveListeners = new ArrayList<>();
    private Class<T> type;

    public ObservableList(final List<T> observed) {
        this.delegate = Objects.requireNonNull(observed);
    }

    public ObservableList() {
        this.delegate = new ArrayList<>();
    }

    @Override
    protected List<T> delegate() {
        return delegate;
    }

    @Override
    public void add(final int index, final T element) {
        fireOnAdd(element);
        super.add(index, element);
    }

    @Override
    public boolean add(final T element) {
        fireOnAdd(element);
        return super.add(element);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends T> elements) {
        elements.forEach(this::fireOnAdd);
        return super.addAll(index, elements);
    }

    @Override
    public boolean remove(final Object object) {
        boolean removed = super.remove(object);
        if (removed) {
            fireOnRemove((T)object);
        }
        return removed;
    }

    @Override
    public T remove(final int index) {
        T removed = super.remove(index);
        fireOnRemove(removed);
        return removed;
    }

    @Override
    public boolean removeAll(final Collection<?> collection) {
        for (Object o : collection) {
            fireOnRemove((T)o);
        }
        return super.removeAll(collection);
    }

    @Override
    public T set(final int index, final T element) {
        T removed = super.set(index, element);
        fireOnRemove(removed);
        fireOnAdd(element);
        return removed;
    }

    @Override
    public boolean addAll(final Collection<? extends T> collection) {
        collection.forEach(this::fireOnAdd);
        return super.addAll(collection);
    }

    public void onRemove(Consumer<T> onRemove) {
        onRemoveListeners.add(onRemove);
    }

    public void onAdd(Consumer<T> onAdd) {
        onAddListeners.add(onAdd);
    }

    private void fireOnAdd(T element){
        onAddListeners.forEach(listener -> listener.accept(element));
    }

    private void fireOnRemove(T element) {
        onRemoveListeners.forEach(listener -> listener.accept(element));
    }

    public ArrayList<Consumer<T>> getOnAddListeners() {
        return onAddListeners;
    }

    public ArrayList<Consumer<T>> getOnRemoveListeners() {
        return onRemoveListeners;
    }
}

