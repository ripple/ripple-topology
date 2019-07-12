package com.ripple.topology.ui.server.config;

import static com.ripple.topology.ui.server.config.JettyUtils.createServlet;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.PostConstruct;

@Configuration
public class TopologyUIRestEndpointConfig {

    private static final Logger logger = LoggerFactory.getLogger(TopologyUIRestEndpointConfig.class);

    @Autowired
    private Environment env;

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Qualifier("topology")
    public Server topologyJettyServer() throws Exception {
        final Server server = new Server();
        server.setHandler(statisticsHandler());
        server.addConnector(serverConnector(server));
        server.setStopTimeout(60_000);
        return server;
    }

    private ServerConnector serverConnector(Server server)
        throws Exception {
        ServerConnector connector = new ServerConnector(server);
        Integer port = env.getRequiredProperty("topology.server.port", Integer.class);
        Integer portOffset = env.getProperty("portOffset", Integer.class, 0);
        connector.setPort(port + portOffset);

        return connector;
    }

    /**
     * Wraps the servlet context handler with a handler that ensures graceful shutdown of in-flight requests.
     */
    private Handler statisticsHandler() {
        final StatisticsHandler statisticsHandler = new StatisticsHandler();
        statisticsHandler.setHandler(handlerCollection());
        return statisticsHandler;
    }

    private HandlerCollection handlerCollection() {
        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.addHandler(servletContextHandler());
        return handlerCollection;
    }

    /**
     * Configures all servlets and filters exposed on this Jetty instance.
     */
    private ServletContextHandler servletContextHandler() {
        final ServletContextHandler servletsHandler = new ServletContextHandler();
        servletsHandler.setContextPath("/");
        servletsHandler.addEventListener(topologyEmbeddedContextListener());
        servletsHandler
            .addServlet(createServlet("topology", dispatcherServlet(), TopologyUIRestWebConfig.class), "/*");

        try {
            servletsHandler.setResourceBase(new ClassPathResource("topology-webapp").getURI().toString());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return servletsHandler;
    }

    private DispatcherServlet dispatcherServlet() {
        DispatcherServlet servlet = new DispatcherServlet();
        servlet.setThrowExceptionIfNoHandlerFound(true);
        return servlet;
    }

    /**
     * Ties the global Application-level spring context together with each Spring Servlet's sub-context in a
     * parent/child relationship.
     */
    @Bean
    public EmbeddedServerServletContextListener topologyEmbeddedContextListener() {
        return new EmbeddedServerServletContextListener();
    }

    @PostConstruct
    public void postConstruct() throws Exception {
        int port = ((ServerConnector) topologyJettyServer().getConnectors()[0]).getLocalPort();
        logger.info("Topology REST Endpoint started on port {}", port);
        System.setProperty("topology.local.rest.port", String.valueOf(port));
    }
}
