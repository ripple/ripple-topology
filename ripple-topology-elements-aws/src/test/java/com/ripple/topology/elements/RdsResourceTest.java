package com.ripple.topology.elements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author matt
 */
public class RdsResourceTest {

    private static final Logger logger = LoggerFactory.getLogger(RdsResourceTest.class);
    private final TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

    @Test
    public void testJavaToYamlDefaultsPreRender() {
        RdsResource resource = new RdsResource("key", "db.t2.micro", 20, "postgres-db", "postgres", "user",
            "passw0rd1");

        Topology topology = new Topology().addElement(resource);

        String result = marshaller.writeAsString(topology);
        logger.info(result);

        String expected = "---\n"
            + "elements:\n"
            + "- type: RdsResource\n"
            + "  key: key\n"
            + "  region: us-west-1\n"
            + "  instanceClass: db.t2.micro\n"
            + "  allocatedStorage: 20\n"
            + "  instanceId: postgres-db\n"
            + "  engine: postgres\n"
            + "  userName: user\n"
            + "  password: passw0rd1\n"
            ;

        assertThat(result, is(expected));
    }

    @Test
    public void testJavaToYamlNonDefaultsPreRender() {
        RdsResource resource = new RdsResource("key", "db.t2.micro", 20, "postgres-db", "postgres", "user",
            "passw0rd1")
            .setPort(5432);

        Topology topology = new Topology().addElement(resource);

        String result = marshaller.writeAsString(topology);
        logger.info(result);

        String expected = "---\n"
            + "elements:\n"
            + "- type: RdsResource\n"
            + "  key: key\n"
            + "  region: us-west-1\n"
            + "  instanceClass: db.t2.micro\n"
            + "  allocatedStorage: 20\n"
            + "  instanceId: postgres-db\n"
            + "  engine: postgres\n"
            + "  userName: user\n"
            + "  password: passw0rd1\n"
            + "  port: 5432\n"
            ;
        assertThat(result, is(expected));
    }

    @Test
    public void testYamlToJava() {
        String input = "---\n"
            + "elements:\n"
            + "- type: RdsResource\n"
            + "  key: key\n"
            + "  instanceClass: db.t2.micro\n"
            + "  allocatedStorage: 20\n"
            + "  instanceId: postgres-db\n"
            + "  engine: postgres\n"
            + "  userName: user\n"
            + "  password: passw0rd1\n"
            + "  port: 5432\n"
            ;

        Topology topology = marshaller.read(input);
        RdsResource resource = topology.getElements(RdsResource.class).get(0);
        assertThat(resource.getKey(), is("key"));
        assertThat(resource.getInstanceClass(), is("db.t2.micro"));
        assertThat(resource.getAllocatedStorage(), is(20));
        assertThat(resource.getInstanceId(), is("postgres-db"));
        assertThat(resource.getEngine(), is("postgres"));
        assertThat(resource.getUserName(), is("user"));
        assertThat(resource.getPassword(), is("passw0rd1"));
        assertThat(resource.getPort(), is(5432));
    }

}
