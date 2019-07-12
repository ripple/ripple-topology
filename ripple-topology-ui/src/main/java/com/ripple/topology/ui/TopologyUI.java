package com.ripple.topology.ui;

import com.ripple.topology.ui.server.config.SpringServer;
import com.ripple.topology.ui.server.config.TopologyUIConfig;
import com.ripple.topology.Topology;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

public class TopologyUI extends SpringServer {

    private final Topology topology;
    private int port = -1;
    private String title = "";

    public TopologyUI(final Topology topology) {
        super(TopologyUIConfig.class);
        this.topology = topology;
    }

    public TopologyUI(final Topology topology, final int port) {
        this(topology);
        this.port = port;
    }

    public TopologyUI(final String title, final Topology topology, final int port) {
        this(topology);
        this.port = port;
        this.title = title;
    }

    @Override
    protected void onBeginStart() {
        topology.start().join();
        AnnotationConfigApplicationContext context = (AnnotationConfigApplicationContext) this.getContext();
        context.getDefaultListableBeanFactory().registerSingleton("topology", topology);
        ConfigurableEnvironment environment = context.getEnvironment();
        Properties properties = new Properties();
        if (port >= 0) {
            properties.setProperty("topology.server.port", String.valueOf(port));
        }
        properties.setProperty("topology.server.name", title);
        environment.getPropertySources().addFirst(new PropertiesPropertySource("node", properties));
        super.onBeginStart();
    }

    @Override
    protected void onBeginShutdown() {
        super.onBeginShutdown();
        topology.stop().join();
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(TopologyUI.class);
    }

    public Topology getTopology() {
        return topology;
    }
}
