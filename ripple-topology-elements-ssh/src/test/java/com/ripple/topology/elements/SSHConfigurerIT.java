package com.ripple.topology.elements;

import static com.ripple.topology.elements.SSHCredentials.withUsernameAndKeyContents;
import static com.ripple.topology.elements.SSHCredentials.withUsernameAndKeyPath;

import com.ripple.topology.Topology;
import com.ripple.topology.io.Content;
import com.ripple.topology.io.ContentLoader;
import com.ripple.topology.io.DefaultContentLoader;
import com.ripple.topology.serialization.TopologyMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.InstanceType;

/**
 * @author matt
 */
public class SSHConfigurerIT {

    private static final Logger logger = LoggerFactory.getLogger(SSHConfigurerIT.class);
    private TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

    @Test(enabled =false, groups = {"manual"})
    public void testDeploy() {
        Topology topology = new Topology()
            .addElement(new Ec2Resource("instance",  "ami-96207fee", InstanceType.T2_MICRO, "ripple-testing", "ripple-testing")
                .setRegion(Region.US_WEST_2.id())
            )
            .addElement(
                new SFTPConfigurer("instance", withUsernameAndKeyPath("ec2-user", "/id_rsa"))
                    .addTransfer("/id_rsa.pub", ".ssh/id_rsa.pub")
            )
            .addElement(new SSHConfigurer("instance", withUsernameAndKeyPath("ec2-user", "/id_rsa"))
                .addCommand("touch foo.txt")
                .addCommand("head /usr/share/dict/words > first_words")
                .addCommand("tail /usr/share/dict/words > last_words")
                .addCommand("ls /")
                .addCommand("cd /tmp; ls")
            )
            ;

        Ec2Resource instance = topology.getResource("instance", Ec2Resource.class);
        logger.info("Before: {}", marshaller.writeAsString(topology));

        topology.startSync();

        logger.info("After: {}", marshaller.writeAsString(topology));

        logger.info("UserData:\n{}", instance.getCalculatedUserData());

        topology.stopSync();
    }

    @Test(enabled =false, groups = {"manual"})
    public void testDeploy2() {
        ContentLoader loader = new DefaultContentLoader();
        Content keyContent = loader.getContent("/id_rsa");
        String key = keyContent.asUTF8String();
        
        Topology topology = new Topology()
            .addElement(new Ec2Resource("instance",  "ami-96207fee", InstanceType.T2_MICRO, "ripple-testing", "ripple-testing")
                .setRegion(Region.US_WEST_2.id())
            )
            .addElement(
                new SFTPConfigurer("instance", withUsernameAndKeyContents("ec2-user", key))
                .addTransfer("classpath:/id_rsa.pub", ".ssh/id_rsa.pub")
            )
            .addElement(new SSHConfigurer("instance", withUsernameAndKeyContents("ec2-user", key))
                .addCommand("cat ~/.ssh/id_rsa.pub")
                .addCommand("head /usr/share/dict/words > first_words")
                .addCommand("tail /usr/share/dict/words > last_words")
                .addCommand("ls /")
                .addCommand("cd /tmp; ls")
            )
            ;

        topology.startSync();

        logger.info("After: {}", marshaller.writeAsString(topology));

        topology.stopSync();
    }
}
