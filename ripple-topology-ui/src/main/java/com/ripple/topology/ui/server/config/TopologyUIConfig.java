package com.ripple.topology.ui.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({
  TopologyUIRestEndpointConfig.class,
})
@PropertySource(value = "classpath:/topology-ui.properties")
public class TopologyUIConfig {

}
