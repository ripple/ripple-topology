package com.ripple.topology.elements;

import static com.ripple.topology.elements.SSHCredentials.withUsernameAndKeyPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class SFTPConfigurerTest {

    private static final Logger logger = LoggerFactory.getLogger(SFTPConfigurerTest.class);
    private static final TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

    @Test
    public void testJavaToYaml() {
        Topology topology = new Topology()
            .addElement(new SFTPConfigurer("instance", withUsernameAndKeyPath("username", "privateKey"))
                .addTransfer("classpath:/id_rsa.pub", "~/.ssh/id_rsa.pub")
                .addTransfer("https://artifactory.ops.ripple.com/artifactory/jvm-libs-release-local/com/ripple/xcurrent/xcurrent-server/4.0.3/xcurrent-server-4.0.3.tar.gz", "/tmp/xcurrent-server-4.0.3.tar.gz")
            )
            ;

        String yaml = marshaller.writeAsString(topology);
        logger.info(yaml);

        String expected = "---\n"
            + "elements:\n"
            + "- type: SFTPConfigurer\n"
            + "  hostKey: instance\n"
            + "  credentials:\n"
            + "    username: username\n"
            + "    privateKeyPath: privateKey\n"
            + "  transfers:\n"
            + "  - source: classpath:/id_rsa.pub\n"
            + "    destination: ~/.ssh/id_rsa.pub\n"
            + "  - source: https://artifactory.ops.ripple.com/artifactory/jvm-libs-release-local/com/ripple/xcurrent/xcurrent-server/4.0.3/xcurrent-server-4.0.3.tar.gz\n"
            + "    destination: /tmp/xcurrent-server-4.0.3.tar.gz\n"
            ;

        assertThat(yaml, is(expected));
    }

    @Test
    public void testYamlToJava() {
        String input = "---\n"
            + "elements:\n"
            + "- type: SFTPConfigurer\n"
            + "  hostKey: instance\n"
            + "  credentials:\n"
            + "    username: username\n"
            + "    privateKeyPath: privateKey\n"
            + "  transfers:\n"
            + "  - source: classpath:/id_rsa.pub\n"
            + "    destination: ~/.ssh/id_rsa.pub\n"
            + "  - source: https://artifactory.ops.ripple.com/artifactory/jvm-libs-release-local/com/ripple/xcurrent/xcurrent-server/4.0.3/xcurrent-server-4.0.3.tar.gz\n"
            + "    destination: /tmp/xcurrent-server-4.0.3.tar.gz\n"
            ;

        Topology topology = marshaller.read(input);
        SFTPConfigurer element = topology.getElements(SFTPConfigurer.class).get(0);
        assertThat(element.getTransfers().size(), is(2));
    }
}
