package com.ripple.topology.elements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.ripple.topology.Topology;
import com.ripple.topology.VariableSource;
import com.ripple.topology.serialization.TopologyMarshaller;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class PropertiesVariableSourceTest {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesVariableSourceTest.class);

    @AfterMethod
    public void afterMethod() {
        System.clearProperty("item1");
    }

    @Test
    public void testLoadFromRoot() {
        PropertiesVariableSource element = new PropertiesVariableSource("/example.properties");
        element.start(null);
        assertThat(element.getVariables().keySet(), hasSize(2));
        assertThat(element.getVariables().get("item1"), is("one"));
        assertThat(element.getVariables().get("item2"), is("two"));
    }

    @Test
    public void testLoadEmpty() {
        PropertiesVariableSource element = new PropertiesVariableSource();
        element.start(null);
        assertThat(element.getVariables().keySet(), hasSize(0));
    }

    @Test
    public void testJavaToYamlDefaults() throws IOException {
        PropertiesVariableSource element = new PropertiesVariableSource();
        Topology topology = new Topology().addElement(element);
        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

        String result = marshaller.writeAsString(topology);

        String expected = "---\n"
            + "elements:\n"
            + "- type: PropertiesVariableSource\n"
            ;

        logger.info(result);
        assertThat(result, is(expected));
    }

    @Test
    public void testJavaToYamlNonDefaultsPreRender() throws IOException {
        System.setProperty("item1", "uno");
        
        VariableSource element = new PropertiesVariableSource()
            .setPropertiesPath("/example.properties")
            .setAllowSystemPropertyOverrides(false)
            .addVariable("item1", "one")
            ;

        Topology topology = new Topology().addElement(element);
        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

        String result = marshaller.writeAsString(topology);

        String expected = "---\n"
            + "elements:\n"
            + "- type: PropertiesVariableSource\n"
            + "  allowSystemPropertyOverrides: false\n"
            + "  propertiesPath: /example.properties\n"
            + "  variables:\n"
            + "    item1: one\n"
            ;

        assertThat(result, is(expected));
    }

    @Test
    public void testJavaToYamlNonDefaultsPostRender() throws IOException {
        System.setProperty("item1", "uno");

        PropertiesVariableSource element = new PropertiesVariableSource()
            .setPropertiesPath("/example.properties")
            .addVariable("item3", "three")
            .addVariable("item2", "nah")
            ;

        Topology topology = new Topology().addElement(element);
        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();
        topology.startSync();

        String result = marshaller.writeAsString(topology);

        String expected = "---\n"
            + "elements:\n"
            + "- type: PropertiesVariableSource\n"
            + "  propertiesPath: /example.properties\n"
            + "  variables:\n"
            + "    item3: three\n"
            + "    item2: two\n"
            + "    item1: uno\n"
            ;

        logger.info(result);
        assertThat(result, is(expected));
    }
}
