package com.ripple.topology;

import com.ripple.topology.elements.EnvironmentConfigurer;
import com.ripple.topology.elements.HostAndPortResource;
import com.ripple.topology.elements.HttpUrlResource;
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
public class RedisTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisTest.class);
    private TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

    @Test(enabled = false)
    public void testJavaApi() throws IOException {
        Topology topology = new Topology()
            .addVariable("topology_id", UUID.randomUUID().toString())
            .addVariable("redis5_version", "5.0.3")
            .addVariable("redis3_version", "3.2")
            .addElement(new EnvironmentConfigurer().addEnvironmentVariable("TEST", "TEST").setApplyToKeysStartingWith("redis"))
            .addElement(
                new NomadJob()
                    .setClusterManager(HttpUrl.parse("http://10.10.10.10:4646"))
                    .setGroupId("redis-${topology_id}")
                    .addElement(
                        new NomadTask("redis5", "redis:${redis5_version}", 6379)
                    )
                    .addElement(
                        new NomadTask("redis3", "redis:${redis3_version}", 6379)
                    )                
            ).start().join();

        logger.info("{}", marshaller.writeAsString(topology));
    }

    @Test(enabled = false)
    public void testYamlApi() throws IOException {
        Topology topology = marshaller.readFromClasspath("/redis.yaml");
        topology.addVariable("topology_id", UUID.randomUUID().toString());
        topology.start().join();
    }

    @Test(enabled = false)
    public void testYamlXCurrent() throws IOException {
        Topology topology = marshaller.readFromClasspath("/xcurrent-h2.yaml");
        topology.addVariable("topology_id", UUID.randomUUID().toString());
        topology.start().join();

        logger.info("{}", marshaller.writeAsString(topology));
    }
}
