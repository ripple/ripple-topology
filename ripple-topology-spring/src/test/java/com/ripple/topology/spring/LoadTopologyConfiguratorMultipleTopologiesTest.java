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

/**
 * @author jfulton
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@LoadTopology.List({
    @LoadTopology(name = "yamlBased", yaml = "classpath:/simple-topology.yaml"),
    @LoadTopology(name = "factoryBased", factory = SimpleTopology.class)
})
@ContextConfiguration(classes = LoadTopologyConfiguratorMultipleTopologiesTest.class)
public class LoadTopologyConfiguratorMultipleTopologiesTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private Topology yamlBased;

    @Autowired
    private Topology factoryBased;

    @Autowired
    private HttpUrlResource sf;

    @Autowired
    private HttpUrlResource ny;

    @Autowired
    private HostAndPortResource db;

    @Autowired
    private HostAndPortResource ldap;

    @Test
    public void test() {
        assertThat(yamlBased, is(notNullValue()));
        assertThat(factoryBased, is(notNullValue()));
        assertThat(sf, is(notNullValue()));
        assertThat(ny, is(notNullValue()));
        assertThat(db, is(notNullValue()));
        assertThat(ldap, is(notNullValue()));
    }
}
