package com.ripple.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.net.HostAndPort;
import com.ripple.topology.elements.HostAndPortResource;
import com.ripple.topology.elements.HttpUrlResource;
import com.ripple.topology.elements.ParallelElementGroup;
import com.ripple.topology.elements.SerialElementGroup;
import com.ripple.topology.elements.StaticHostAndPort;
import com.ripple.topology.elements.StaticHttpUrl;
import com.ripple.topology.serialization.TopologyMarshaller;
import java.io.IOException;
import java.util.UUID;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class TopologyMarshallerTest {

    private static final Logger logger = LoggerFactory.getLogger(TopologyMarshallerTest.class);

    @Test
    public void testSerialize() throws IOException {
        Topology topology = new Topology();
        topology.addVariable("host", "${key}.${topology_id}.testnet.ripple.com");
//            .addVariable("host", "localhost")
        topology.addVariable("topology_id", UUID.randomUUID().toString());
        topology.addElement(new StaticHttpUrl("sf", HttpUrl.parse("http://${host}")));
        topology.addElement(new StaticHostAndPort("ny", HostAndPort.fromParts("${host}", 5000)));
        topology.addElement(new ParallelElementGroup()
            .addElement(new StaticHttpUrl("la", HttpUrl.parse("http://${host}")))
            .addElement(new StaticHostAndPort("ho", HostAndPort.fromString("${host}:5432")))
        );
        topology.addElement(new SerialElementGroup().addElement(new ParallelElementGroup()
                .addElement(new StaticHttpUrl("wa", HttpUrl.parse("http://${host}")))
                .addElement(new StaticHostAndPort("dl", HostAndPort.fromString("${host}:5432")))
            )
        );
        topology.start().join();

        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

        logger.info("{}", marshaller.writeAsString(topology));
    }

    @Test
    public void testDeserialize() throws IOException {

        String yaml = "---\n"
            + "elements:\n"
            + "- type: \"StaticHttpUrl\"\n"
            + "  key: \"sf\"\n"
            + "  httpUrl: \"http://localhost/\"\n"
            + "- type: \"StaticHostAndPort\"\n"
            + "  key: \"ny\"\n"
            + "  hostAndPort: \"localhost:5000\"\n"
            + "- type: \"parallel\"\n"
            + "  elements:\n"
            + "  - type: \"StaticHttpUrl\"\n"
            + "    key: \"la\"\n"
            + "    httpUrl: \"http://localhost/\"\n"
            + "  - type: \"StaticHostAndPort\"\n"
            + "    key: \"postgres\"\n"
            + "    hostAndPort: \"localhost:5432\"";

        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();
        Topology topology = marshaller.read(yaml);

        assertThat(topology.getResourceOptional("sf", HttpUrlResource.class).isPresent(), is(true));
        assertThat(topology.getResourceOptional("ny", HostAndPortResource.class).isPresent(), is(true));
        assertThat(topology.getResourceOptional("la", HttpUrlResource.class).isPresent(), is(true));
        assertThat(topology.getResourceOptional("postgres", HostAndPortResource.class).isPresent(), is(true));
        topology.start().join();
    }
}
