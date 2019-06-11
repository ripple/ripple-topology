package com.ripple.topology.elements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.net.HostAndPort;
import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * @author jfulton
 */
public class Ec2ResourceTest {

    private static final Logger logger = LoggerFactory.getLogger(Ec2ResourceTest.class);
    private final TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

    @Test
    public void testJavaToYamlDefaultsPreRender() {
        Ec2Resource resource = new Ec2Resource("key", "ami-1234", InstanceType.T1_MICRO, "keypair", "security-group");

        Topology topology = new Topology().addElement(resource);

        String result = marshaller.writeAsString(topology);
        logger.info(result);

        String expected = "---\n"
            + "elements:\n"
            + "- type: Ec2Resource\n"
            + "  key: key\n"
            + "  ami: ami-1234\n"
            + "  keyPair: keypair\n"
            + "  securityGroup: security-group\n"
            ;

        assertThat(result, is(expected));
    }

    @Test
    public void testJavaToYamlNonDefaultsPreRender() {
        Ec2Resource resource = new Ec2Resource("key", "ami-1234", InstanceType.T1_MICRO, "keypair", "security-group")
            .setRegion("AP_NORTHEAST_1")
            .setInstanceType(InstanceType.A1_2_XLARGE)
            ;

        Topology topology = new Topology().addElement(resource);

        String result = marshaller.writeAsString(topology);
        logger.info(result);

        String expected = "---\n"
            + "elements:\n"
            + "- type: Ec2Resource\n"
            + "  key: key\n"
            + "  ami: ami-1234\n"
            + "  instanceType: A1_2_XLARGE\n"
            + "  region: AP_NORTHEAST_1\n"
            + "  keyPair: keypair\n"
            + "  securityGroup: security-group\n"
            ;

        assertThat(result, is(expected));
    }

    @Test
    public void testYamlToJava() {
        String input = "---\n"
            + "elements:\n"
            + "- type: \"Ec2Resource\"\n"
            + "  key: \"key\"\n"
            + "  ami: \"ami-1234\"\n"
            + "  instanceType: \"A1_2_XLARGE\"\n"
            + "  region: \"AP_NORTHEAST_1\"\n"
            + "  keyPair: \"keypair\"\n"
            + "  securityGroup: \"security-group\"\n"
            ;

        Topology topology = marshaller.read(input);
        Ec2Resource resource = topology.getElements(Ec2Resource.class).get(0);
        assertThat(resource.getRegion(), is("AP_NORTHEAST_1"));
        assertThat(resource.getAmi(), is("ami-1234"));
        assertThat(resource.getInstanceType(), is(InstanceType.A1_2_XLARGE));
        assertThat(resource.getKeyPair(), is("keypair"));
        assertThat(resource.getSecurityGroup(), is("security-group"));
    }

    @Test
    public void testVariableResolution() {
        Topology topology = new Topology();
        topology.addElement(new StaticHostAndPort("sf-db", HostAndPort.fromString("localhost:5432")));
        topology.addElement(new StaticHostAndPort("sf-ldap", HostAndPort.fromString("localhost:389")));
        Ec2Resource resource = new Ec2Resource("sf", "ami-1234", InstanceType.A1_2_XLARGE, "keyPair", "securityGroup")
            .addUserData("mkdir /opt/application && cd /opt/application")
            .addUserData("java -cp \"./etc:./lib/*\" $JAVA_MEM $JAVA_OPTS com.ripple.application.Main")
            .setDryRun(true)
            ;
        resource.addProperty("xcurrent.db.url", "jdbc:postgres//${sf-db.hostAndPort}");
        resource.addProperty("xcurrent.ldap.url", "${sf-ldap.hostAndPort}");
        resource.addEnvironmentVariable("RUNTIME_MODE", "prod");
        topology.addElement(resource);
        topology.startSync();

        String yaml = marshaller.writeAsString(topology);

        String expected = "---\n"
            + "elements:\n"
            + "- type: StaticHostAndPort\n"
            + "  key: sf-db\n"
            + "  hostAndPort: localhost:5432\n"
            + "- type: StaticHostAndPort\n"
            + "  key: sf-ldap\n"
            + "  hostAndPort: localhost:389\n"
            + "- type: Ec2Resource\n"
            + "  key: sf\n"
            + "  properties:\n"
            + "    xcurrent.db.url: jdbc:postgres//localhost:5432\n"
            + "    xcurrent.ldap.url: localhost:389\n"
            + "  environment:\n"
            + "    RUNTIME_MODE: prod\n"
            + "  ami: ami-1234\n"
            + "  instanceType: A1_2_XLARGE\n"
            + "  keyPair: keyPair\n"
            + "  securityGroup: securityGroup\n"
            + "  userData:\n"
            + "  - mkdir /opt/application && cd /opt/application\n"
            + "  - java -cp \"./etc:./lib/*\" $JAVA_MEM $JAVA_OPTS com.ripple.application.Main\n"
            ;

        logger.info(yaml);
        assertThat(yaml, is(expected));

        String expectedUserData = "#!/usr/bin/env bash\n"
            + "\n"
            + "export RUNTIME_MODE=\"prod\"\n"
            + "export JAVA_MEM=\"-Xmx512m -Xms256m\"\n"
            + "export JAVA_OPTS=\"-Dxcurrent.db.url=jdbc:postgres//localhost:5432 -Dxcurrent.ldap.url=localhost:389\"\n"
            + "mkdir /opt/application && cd /opt/application\n"
            + "java -cp \"./etc:./lib/*\" $JAVA_MEM $JAVA_OPTS com.ripple.application.Main\n"
            ;

        logger.info(resource.getCalculatedUserData());
        assertThat(resource.getCalculatedUserData(), is(expectedUserData));
    }
}
