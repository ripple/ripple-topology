package com.ripple.topology.elements;

import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class VariablesConfigurerTest {

    private static final Logger logger = LoggerFactory.getLogger(VariablesConfigurerTest.class);

    @Test
    public void testJavaToYaml() throws IOException {
        Topology topology = new Topology();
        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

        topology.addElement(new VariablesConfigurer().addVariable("one", "uno").addVariable("two", "dos"));

        logger.info("{}", marshaller.writeAsString(topology));
    }
}
