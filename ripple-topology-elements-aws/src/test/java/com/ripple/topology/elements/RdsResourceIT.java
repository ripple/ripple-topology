package com.ripple.topology.elements;

import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author matt
 */
public class RdsResourceIT {

    private static final Logger logger = LoggerFactory.getLogger(RdsResourceIT.class);

    @Test(enabled = false, groups = {"manual"})
    public void testDeploy() {

        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

        Topology topology = new Topology()
            .addElement(new RdsResource("instance", "db.t2.micro", 20, "postgres-db", "postgres", "test_user",
                "passw0rd1")
                .setPort(5432)
            )
            ;

        logger.info("Before: {}", marshaller.writeAsString(topology));

        topology.startSync();

        logger.info("After: {}", marshaller.writeAsString(topology));

        topology.stopSync();
    }

}
