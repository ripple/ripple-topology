package com.ripple.topology.elements;

import com.google.common.net.HostAndPort;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import com.ripple.topology.Topology;
import com.ripple.topology.VariableResolver;
import com.ripple.topology.VariableResolverAware;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfulton
 */
public class SFTPConfigurer extends AbstractSSHConfigurer<SFTPConfigurer> implements VariableResolverAware {

    private static final Logger logger = LoggerFactory.getLogger(SFTPConfigurer.class);
    private final List<SFTPFileTransfer> transfers = new ArrayList<>();

    public SFTPConfigurer() {
        // Jackson Only
    }

    public SFTPConfigurer(final String hostKey, SSHCredentials credentials) {
        super(hostKey, credentials);
    }

    @Override
    public void configure(final Topology topology) {
        HostAndPort hostAndPort = calculateHostAndPort(topology);

        Session session = waitForSession(topology, hostAndPort, Duration.ofSeconds(90), Duration.ofMillis(500));

        if (session == null) {
            return;
        }

        try {
            for (SFTPFileTransfer transfer : getTransfers()) {
                ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();

                String source = transfer.getSource();
                String destination = transfer.getDestination();

                try (InputStream inputStream = topology.contentLoader().getContent(source).getInputStream()) {
                    try {
                        logger.info("$> scp '{}' '{}'", source, destination);
                        channel.put(inputStream, destination);
                    } catch (SftpException e) {
                        logger.error("Error copying {} to {}", source, destination);
                        throw new RuntimeException(e);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    channel.disconnect();
                }
            }
        } catch (JSchException e) {
            logger.error("Error establishing SFTP channel", e);
        } finally {
            session.disconnect();
        }
    }

    public List<SFTPFileTransfer> getTransfers() {
        return transfers;
    }

    public SFTPConfigurer addTransfer(String source, String destination) {
        getTransfers().add(new SFTPFileTransfer(source, destination));
        return this;
    }

    @Override
    public void resolveVariables(final Topology topology, final VariableResolver resolver) {
        for (SFTPFileTransfer transfer : transfers) {
            transfer.setSource(resolver.resolve(transfer.getSource()));
            transfer.setDestination(resolver.resolve(transfer.getDestination()));
        }
    }

    final class EasyRepo implements HostKeyRepository {

        @Override
        public int check(final String host, final byte[] bkey) {
            return HostKeyRepository.OK;
        }

        @Override
        public void add(final HostKey hostkey, final UserInfo info) {
            // do nothing
        }

        @Override
        public void remove(final String host, final String type) {
            // do nothing
        }

        @Override
        public void remove(final String host, final String type,
            final byte[] bkey) {
            // do nothing
        }

        @Override
        public String getKnownHostsRepositoryID() {
            return "";
        }
        @Override
        public HostKey[] getHostKey() {
            return new HostKey[0];
        }

        @Override
        public HostKey[] getHostKey(final String host, final String type) {
            return new HostKey[0];
        }

    }
}
