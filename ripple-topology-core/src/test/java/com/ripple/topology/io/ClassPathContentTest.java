package com.ripple.topology.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class ClassPathContentTest {

    @Test
    public void testExistsExisting() {
        assertThat(existing().exists(), is(true));
    }

    @Test
    public void testExistsMissing() {
        assertThat(missing().exists(), is(false));
    }

    @Test
    public void testDescriptionExisting() {
        assertThat(existing().getDescription(), is("class path resource [example.properties]"));
    }

    @Test
    public void testDescriptionMissing() {
        assertThat(missing().getDescription(), is("class path resource [missing.properties]"));
    }

    private ClassPathContent existing() {
        return new ClassPathContent("/example.properties");
    }

    private ClassPathContent missing() {
        return new ClassPathContent("/missing.properties");
    }
}
