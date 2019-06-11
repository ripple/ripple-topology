package com.ripple.topology.elements;

import static com.ripple.topology.elements.SSHCredentials.withUsernameAndKeyContents;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class SSHConfigurerTest {

    private static final Logger logger = LoggerFactory.getLogger(SSHConfigurerTest.class);
    private final static TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

    @Test
    public void testJavaToYamlDefaultConstructor() {
        Topology topology = new Topology()
            .addElement(new SSHConfigurer()
                .addCommand("rm -rf /*")
            )
            ;

        String yaml= marshaller.writeAsString(topology);
        logger.info(yaml);

        String expectedYaml = "---\n"
            + "elements:\n"
            + "- type: SSHConfigurer\n"
            + "  commands:\n"
            + "  - rm -rf /*\n"
            ;

        assertThat(yaml, is(expectedYaml));
    }

    @Test
    public void testJavaToYamlRequiredConstructor() {
        Topology topology = new Topology()
            .addElement(new SSHConfigurer("sf", withUsernameAndKeyContents("bob", "xYa12ReZb"))
                .addCommand("cd /")
                .addCommand("rm -rf *")
            )
            ;

        String yaml= marshaller.writeAsString(topology);
        logger.info(yaml);

        String expectedYaml = "---\n"
            + "elements:\n"
            + "- type: SSHConfigurer\n"
            + "  hostKey: sf\n"
            + "  credentials:\n"
            + "    username: bob\n"
            + "    privateKeyContents: xYa12ReZb\n"
            + "  commands:\n"
            + "  - cd /\n"
            + "  - rm -rf *\n"
            ;

        assertThat(yaml, is(expectedYaml));
    }

    @Test
    public void testYamlToJava() {
        String input = "---\n"
            + "elements:\n"
            + "- type: SSHConfigurer\n"
            + "  hostKey: sf\n"
            + "  credentials:\n"
            + "    username: bob\n"
            + "    privateKeyContents: xYa12ReZb\n"
            + "  commands:\n"
            + "  - cd /\n"
            + "  - rm -rf *\n"
            ;

        Topology topology = marshaller.read(input);
        SSHConfigurer configurer = topology.getElements(SSHConfigurer.class).get(0);
        assertThat(configurer.getHostKey(), is("sf"));
        assertThat(configurer.getCredentials().getUsername(), is("bob"));
        assertThat(configurer.getCredentials().getPrivateKeyContents(), is("xYa12ReZb"));
        assertThat(configurer.getCommands(), contains("cd /", "rm -rf *"));

    }
}
