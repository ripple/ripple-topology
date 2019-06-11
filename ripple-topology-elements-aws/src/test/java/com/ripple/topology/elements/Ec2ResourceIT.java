package com.ripple.topology.elements;

import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * @author jfulton
 */
public class Ec2ResourceIT {

    private static final Logger logger = LoggerFactory.getLogger(Ec2ResourceIT.class);

    @Test(enabled = false, groups = {"manual"})
    public void testDeploy() {

        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

        Topology topology = new Topology()
            .addElement(new Ec2Resource("instance",  "ami-96207fee", InstanceType.T2_MICRO, "ripple-testing", "ripple-testing")
                .setRegion(Region.US_WEST_2.id())
                .addEnvironmentVariable("RUNTIME_MODE", "prod")
                .addProperty("xcurrent.db.ur", "jdpc:postgres//localhost:5432")
                .addUserData("echo RUNTIME_MODE=$RUNTIME_MODE")
                .addUserData("echo $RUNTIME_MODE > /tmp/RUNTIME_MODE")
                .addUserData("echo JAVA_MEM=$JAVA_MEM")
                .addUserData("echo $JAVA_MEM > /tmp/JAVA_MEM")
                .addUserData("echo $JAVA_OPTS > /tmp/JAVA_OPTS")
            )
            ;

        Ec2Resource instance = topology.getResource("instance", Ec2Resource.class);
        logger.info("Before: {}", marshaller.writeAsString(topology));

        topology.startSync();

        logger.info("After: {}", marshaller.writeAsString(topology));

        logger.info("UserDate: {}", instance.getCalculatedUserData());

        topology.stopSync();
    }
}
