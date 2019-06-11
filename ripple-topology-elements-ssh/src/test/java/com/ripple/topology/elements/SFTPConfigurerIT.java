package com.ripple.topology.elements;

import static com.ripple.topology.elements.SSHCredentials.withUsernameAndKeyContents;
import static com.ripple.topology.elements.SSHCredentials.withUsernameAndKeyPath;

import com.ripple.topology.Topology;
import com.ripple.topology.io.Content;
import com.ripple.topology.io.ContentLoader;
import com.ripple.topology.io.DefaultContentLoader;
import com.ripple.topology.serialization.TopologyMarshaller;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.utils.IoUtils;

/**
 * @author matt
 */
public class SFTPConfigurerIT {

    private static final Logger logger = LoggerFactory.getLogger(SFTPConfigurerIT.class);
    TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

    @Test(enabled = false, groups = {"manual"})
    public void testSFTPWithKeyPath() {
        Topology topology = new Topology()
            .addElement(new Ec2Resource("instance",  "ami-96207fee", InstanceType.T2_MICRO, "ripple-testing", "ripple-testing")
                .setRegion(Region.US_WEST_2.id())
                .addUserData("sudo yum install -y java-1.8.0")
            )
            .addElement(new SFTPConfigurer("instance", withUsernameAndKeyPath("ec2-user", "/id_rsa"))
                .addTransfer("classpath:/id_rsa.pub", ".ssh/id_rsa.pub")
                .addTransfer("https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-logback.xml",
                    "/tmp/logback.xml")
                .addTransfer("https://artifactory.ops.ripple.com/artifactory/jvm-libs-release/com/ripple/service/"
                    + "example/example-service-server/1.2.0/example-service-server-1.2.0.tar.gz", "/tmp/example.tar.gz")
            )
            .addElement(new SSHConfigurer("instance", withUsernameAndKeyPath("ec2-user", "/id_rsa"))
                .addCommand("cd /tmp && tar xvzf example.tar.gz && cd example-service-server-1.2.0 &&"
                    + "JAVA_OPTS=\"-Ddb=h2 -DRUNTIME_MODE=dev\" bin/service start")
            );

        Ec2Resource instance = topology.getResource("instance", Ec2Resource.class);
        logger.info("Before: {}", marshaller.writeAsString(topology));

        topology.startSync();

        logger.info("After: {}", marshaller.writeAsString(topology));

        logger.info("UserData: {}", instance.getCalculatedUserData());

        topology.stopSync();

    }

    @Test(enabled = false, groups = {"manual"})
    public void testSFTPWithKeyContents() {
        ContentLoader loader = new DefaultContentLoader();
        Content keyContent = loader.getContent("/id_rsa");
        String key;
        try (InputStream keyStream = keyContent.getInputStream()) {
            key = IoUtils.toUtf8String(keyStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Topology topology = new Topology()
            .addElement(new Ec2Resource("instance",  "ami-96207fee", InstanceType.T2_MICRO, "ripple-testing", "ripple-testing")
                .setRegion(Region.US_WEST_2.id())
            )
            .addElement(new SFTPConfigurer("instance", withUsernameAndKeyContents("ec2-user", key))
                .addTransfer("/id_rsa.pub", ".ssh/id_rsa.pub")
                .addTransfer("https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-logback.xml",
                    "/tmp/logback.xml")
            )
            ;

        topology.startSync();

        logger.info("After: {}", marshaller.writeAsString(topology));

        topology.stopSync();

    }
}
