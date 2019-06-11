package com.ripple.topology.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class ObservableListTest {

    private static final Logger logger = LoggerFactory.getLogger(ObservableListTest.class);

    @Test
    public void testAdd() {
        AtomicBoolean called = new AtomicBoolean(false);
        String message = "Hello";
        ObservableList<String> list = new ObservableList<>(new ArrayList<>());
        list.onAdd(s -> {
            assertThat(s.equals(message), is(true));
            called.set(true);
        });
        list.add(message);
        assertThat(called.get(), is(true));
    }

    @Test
    public void testAddIndex() {
        AtomicBoolean called = new AtomicBoolean(false);
        String message = "message2";
        List<String> delegate = new ArrayList<>();
        delegate.add("message1");
        ObservableList<String> list = new ObservableList<>(delegate);
        list.onAdd(s -> {
            assertThat(s.equals(message), is(true));
            called.set(true);
        });
        list.add(0, message);
        assertThat(called.get(), is(true));
        assertThat(list.size(), is(2));
    }

    @Test
    public void testAddAll() {
        AtomicInteger calls = new AtomicInteger(0);
        Set<String> input = new HashSet<>();
        input.add("message1");
        input.add("message2");
        ObservableList<String> list = new ObservableList<>(new ArrayList<>());
        list.onAdd(s -> {
            assertThat(input.contains(s), is(true));
            calls.incrementAndGet();
        });
        list.addAll(input);
        assertThat(calls.get(), is(2));
    }

    @Test
    public void testAddAllIndex() {
        AtomicInteger calls = new AtomicInteger(0);
        Set<String> input = new HashSet<>();
        input.add("message3");
        input.add("message4");
        List<String> delegate = new ArrayList<>();
        delegate.add("message1");
        delegate.add("message2");
        ObservableList<String> list = new ObservableList<>(delegate);
        list.onAdd(s -> {
            assertThat(input.contains(s), is(true));
            calls.incrementAndGet();
        });
        list.addAll(0, input);
        assertThat(calls.get(), is(2));
        assertThat(list.size(), is(4));
    }

    @Test
    public void testSet() {
        AtomicInteger addCount = new AtomicInteger(0);
        AtomicInteger removeCount = new AtomicInteger(0);
        ObservableList<String> list = new ObservableList<>();
        list.add("message1");
        list.add("message2");
        list.onRemove(s -> {
            assertThat(s, is("message1"));
            addCount.incrementAndGet();
        });
        list.onAdd(s -> {
           assertThat(s, is("message1"));
           removeCount.incrementAndGet();
        });
        list.set(0, "message1");
        assertThat(addCount.get(), is(1));
        assertThat(removeCount.get(), is(1));
    }

    @Test
    public void testRemove() {
        AtomicInteger removed = new AtomicInteger(0);
        List<String> delegate = new ArrayList<>();
        delegate.add("message");
        ObservableList<String> list = new ObservableList<>(delegate);
        list.onRemove(s -> {
            assertThat(s, is("message"));
            removed.incrementAndGet();
        });
        list.remove("message");
        assertThat(removed.get(), is(1));
        assertThat(list.size(), is(0));
    }

    @Test
    public void testRemoveIndex() {
        AtomicInteger removed = new AtomicInteger(0);
        List<String> delegate = new ArrayList<>();
        delegate.add("message1");
        delegate.add("message2");
        ObservableList<String> list = new ObservableList<>(delegate);
        list.onRemove(s -> {
            assertThat(s, is("message1"));
            removed.incrementAndGet();
        });
        list.remove(0);
        assertThat(removed.get(), is(1));
        assertThat(list.size(), is(1));
    }

    @Test
    public void testRemoveAll() {
        Set<String> input = new HashSet<>();
        input.add("message1");
        input.add("message2");

        List<String> delegate = new ArrayList<>(input);
        delegate.add("message3");
        delegate.add("message4");

        AtomicInteger removed = new AtomicInteger(0);

        ObservableList<String> list = new ObservableList<>(delegate);
        list.onRemove(s -> {
            assertThat(input.contains(s), is(true));
            removed.incrementAndGet();
        });
        list.removeAll(input);
        assertThat(removed.get(), is(2));
        assertThat(list.size(), is(2));
    }

    @Test
    public void test() {
        ObservableList<String> list = new ObservableList<>();
        list.onAdd(item -> logger.info("Added {}", item));
        list.onAdd(item -> logger.info("Logged {} twice :-(", item));

        list.add("One");
        list.add("Two");
    }
}
