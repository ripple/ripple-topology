package com.ripple.topology.elements;

import com.google.common.base.Charsets;
import com.google.common.net.HostAndPort;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.ripple.topology.Topology;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfulton
 * @author mphinney
 */
public class SSHConfigurer extends AbstractSSHConfigurer<SSHConfigurer> {

    private static final Logger logger = LoggerFactory.getLogger(SSHConfigurer.class);
    private final List<String> commands = new ArrayList<>();

    public SSHConfigurer() {
        // Jackson Only
    }

    public SSHConfigurer(final String hostKey, SSHCredentials credentials) {
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
            for (String command : commands) {
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                channel.setInputStream(null);
                channel.setOutputStream(outputStream);
                channel.setErrStream(outputStream);
                channel.setCommand(command);
                channel.connect();
                int code = code(session, channel);
                String output = outputStream.toString(Charsets.UTF_8.toString());
                channel.disconnect();
                
                if (!output.equals("")) {
                    logger.info("$> {} ({})\n{}", command, code, output);
                } else {
                    logger.info("$> {} ({})", command, code);
                }
            }
        } catch (Exception e) {
            logger.error("Error establishing SSH exec channel");
        } finally {
            session.disconnect();
        }
    }

    public List<String> getCommands() {
        return commands;
    }

    public SSHConfigurer addCommand(String command) {
        getCommands().add(Objects.requireNonNull(command));
        return this;
    }

    private int code(final Session session, final ChannelExec exec) throws IOException {
        while (!exec.isClosed()) {
            try {
                session.sendKeepAliveMsg();
            } catch (final Exception ex) {
                throw new IOException(ex);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(250L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
        }
        return exec.getExitStatus();
    }
}
