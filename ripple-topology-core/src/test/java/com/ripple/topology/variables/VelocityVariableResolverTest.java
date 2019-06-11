package com.ripple.topology.variables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class VelocityVariableResolverTest {

    @Test
    public void testResolveRecursive() {
        VelocityVariableResolver resolver = new VelocityVariableResolver();
        resolver.put("adjective", "cruel");
        resolver.put("message", "Hello, ${adjective}");
        assertThat(resolver.resolve("$message world!"), is("Hello, cruel world!"));
    }
}
