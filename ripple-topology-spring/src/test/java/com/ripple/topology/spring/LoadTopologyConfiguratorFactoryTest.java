package com.ripple.topology.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.ripple.topology.Topology;
import com.ripple.topology.elements.HostAndPortResource;
import com.ripple.topology.elements.HttpUrlResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@LoadTopology(name = "topology", factory = SimpleTopology.class)
@ContextConfiguration(classes = { LoadTopologyConfiguratorFactoryTest.class })
public class LoadTopologyConfiguratorFactoryTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private Topology topology;

    @Autowired
    private HttpUrlResource sf;

    @Autowired
    private HostAndPortResource db;

    @Test
    public void test() {
        assertThat(topology, is(notNullValue()));
        assertThat(sf, is(notNullValue()));
        assertThat(db, is(notNullValue()));
    }
}

