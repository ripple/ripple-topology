package com.ripple.topology.elements;

import static org.hamcrest.MatcherAssert.assertThat;

import com.ripple.topology.Topology;
import org.hamcrest.core.Is;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class EnvironmentConfigurerTest {

    @Test
    public void testVariableResolution() {

        Topology topology = new Topology();
        topology.addVariable("globalVar1", "one");

        EnvironmentConfigurer configurer = new EnvironmentConfigurer()
            .addVariable("globalVar2", "two, ${globalVar1}")
            .addEnvironmentVariable("prop1", "${globalVar2}")
            .addEnvironmentVariable("prop2", "${globalVar2}")
            .setApplyToKeysEndingWith("-db")
            ;

        topology.addElement(configurer);

        TestResource testResource = new TestResource();
        testResource.addEnvironmentVariable("prop1", "one, two");

        topology.addElement(testResource);

        topology.start().join();

        assertThat(topology.getElements(EnvironmentConfigurer.class).get(0).getEnvironment().get("prop2"), Is.is("two, one"));
        assertThat(topology.getElements(TestResource.class).get(0).getEnvironment().get("prop1"), Is.is("one, two"));
        assertThat(topology.getElements(TestResource.class).get(0).getEnvironment().get("prop2"), Is.is("two, one"));
    }

    public static class TestResource extends AbstractEnvironmentAwareResource {

        public TestResource(final String key) {
            super(key);
        }

        public TestResource() {
            super("example-db");
        }
    }
}
