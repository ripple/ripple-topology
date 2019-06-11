package com.ripple.topology.ui.server.config;

import com.ripple.spring.config.RuntimeModePropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * @author UI Archetype
 */
@Configuration
@Import({
    TopologyUIRestEndpointConfig.class,
})
@PropertySource(value = "classpath:/topology-ui.properties", factory = RuntimeModePropertySourceFactory.class)
public class TopologyUIConfig {

}
