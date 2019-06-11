package com.ripple.topology.elements;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.net.HostAndPort;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.ripple.topology.Resource;
import com.ripple.topology.Topology;
import com.ripple.topology.utils.HealthUtils;
import java.io.File;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfulton
 */
@SuppressWarnings("unchecked")
@JsonPropertyOrder({"hostKey", "username", "privateKey"})
public abstract class AbstractSSHConfigurer<T extends AbstractSSHConfigurer<T>> implements Configurer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSSHConfigurer.class);
    private String hostKey;
    private SSHCredentials credentials;

    public AbstractSSHConfigurer() {
        // Jackson Only
    }

    public AbstractSSHConfigurer(final String hostKey, SSHCredentials credentials) {
        this.hostKey = Objects.requireNonNull(hostKey);
        this.credentials = Objects.requireNonNull(credentials);
    }

    public String getHostKey() {
        return hostKey;
    }

    public T setHostKey(final String hostKey) {
        this.hostKey = Objects.requireNonNull(hostKey);
        return (T) this;
    }

    public SSHCredentials getCredentials() {
        return credentials;
    }

    public T setCredentials(final SSHCredentials credentials) {
        this.credentials = Objects.requireNonNull(credentials);
        return (T) this;
    }

    protected HostAndPort calculateHostAndPort(Topology topology) {
        Resource resource = topology.getResource(getHostKey(), Resource.class);
        HostAndPort hostAndPort;
        if (resource instanceof SSHHostAndPortResource) {
            hostAndPort = ((SSHHostAndPortResource) resource).getSSHHostAndPort();
        } else if (resource instanceof HostResource) {
            hostAndPort = HostAndPort.fromParts(((HostResource) resource).getHost(), 22);
        } else {
            throw new RuntimeException(
                "SSH only works on Resources of type SSHHostAndPortResource, or HostResource defaulting to port 22");
        }
        return hostAndPort;
    }

    protected Session waitForSession(Topology topology, HostAndPort hostAndPort, Duration maxWait, Duration retryPause) {
        final String host = hostAndPort.getHost();
        final int port = hostAndPort.getPort();
        final String username = credentials.getUsername();
        final File file = credentials.calculateKeyFile(topology.contentLoader());

        JSch.setConfig("StrictHostKeyChecking", "no");

        AtomicReference<Session> sessionReference = new AtomicReference<>();
        HealthUtils.waitForHealth(maxWait, retryPause, "'" + getHostKey() + "' SSH Deamon", () -> {
            try {
                JSch jSch = new JSch();
                jSch.addIdentity(file.getAbsolutePath());
                jSch.setHostKeyRepository(new VoidRepo());
                Session session = jSch.getSession(username, host, port);
                session.setServerAliveInterval(
                    (int) TimeUnit.SECONDS.toMillis(10)
                );
                session.setServerAliveCountMax(1000000);
                session.connect();
                sessionReference.set(session);
                return true;
            } catch (JSchException e) {
                return false;
            }
        });

        if (sessionReference.get() != null) {
            logger.info("SSH Connection established to {}", hostAndPort);
        } else {
            logger.warn("SSH Connection to {} could not be established within {} millis", hostAndPort, maxWait.toMillis());
        }
        return sessionReference.get();
    }

    final class VoidRepo implements HostKeyRepository {

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
