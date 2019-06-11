package com.ripple.topology;

import com.ripple.topology.elements.Configurer;
import com.ripple.topology.elements.HostAndPortResource;
import com.ripple.topology.elements.PropertiesAwareResource;

/**
 * @author jfulton
 */
public class DatabaseConfigurer implements Configurer {

    private final String databaseResource;
    private final String applicationResource;
    private final String databaseKey;

    public DatabaseConfigurer(final String databaseResource, final String applicationResource, final String databaseKey) {
        this.databaseResource = databaseResource;
        this.applicationResource = applicationResource;
        this.databaseKey = databaseKey;
    }

    @Override
    public void configure(final Topology topology) {
        HostAndPortResource database = topology.getResource(databaseResource, HostAndPortResource.class);
        PropertiesAwareResource application = topology.getResource(applicationResource, PropertiesAwareResource.class);
        application.getProperties().put(databaseKey, database.getHostAndPort().toString());
    }
}
