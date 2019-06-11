package com.ripple.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.fail;

import com.google.common.net.HostAndPort;
import com.ripple.topology.elements.HostAndPortResource;
import com.ripple.topology.elements.HttpUrlResource;
import com.ripple.topology.elements.ParallelElementGroup;
import com.ripple.topology.elements.PropertiesAwareResource;
import com.ripple.topology.elements.PropertiesVariableSource;
import com.ripple.topology.elements.SerialElementGroup;
import com.ripple.topology.elements.StaticHostAndPort;
import com.ripple.topology.elements.StaticHttpUrl;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author Library Archetype
 */
public class TopologyTest {

    private static final Logger logger = LoggerFactory.getLogger(TopologyTest.class);

    @Test
    public void test() {
        Topology topology = new Topology().registerShutdownHook();
        topology.getVariables().put("db", "h2");

        ParallelElementGroup group = new ParallelElementGroup();
        group.addElement(new XCurrentResource("las").addProperty("db", "${db}"));
        group.addElement(new XCurrentResource("sfo"));
        topology.addElement(group);

        topology.addElement(new StaticHostAndPort("postgres", HostAndPort.fromParts("localhost", 5432)));
        topology.addElement(new DatabaseConfigurer("postgres", "sfo", "xcurrent.database.endpoint"));
        topology.addElement(new XCurrentResource("sfo"));

        topology.start().thenRun(() -> {
            assertThat(topology.getResource("postgres", HostAndPortResource.class).getHostAndPort().toString(),
                is("localhost:5432"));
            assertThat(topology.getResource("sfo", PropertiesAwareResource.class)
                .getProperties().get("xcurrent.database.endpoint"), is("localhost:5432"));
        }).join();

        topology.stop().join();
    }

    @Test
    public void testRecursiveElementGroup() {
        Topology topology = new Topology();

        SerialElementGroup level1 = new SerialElementGroup();
        topology.getElements().add(level1);
        level1.addElement(new StaticHttpUrl("example", HttpUrl.parse("http://localhost")));

        assertThat(topology.getResourceOptional("example", HttpUrlResource.class).isPresent(), is(true));

        SerialElementGroup level2 = new SerialElementGroup();
        level1.addElement(level2
            .addElement(new StaticHostAndPort("example2", HostAndPort.fromString("localhost:80"))));

        assertThat(topology.getResourceOptional("example2", HostAndPortResource.class).isPresent(), is(true));

        SerialElementGroup level3 = new SerialElementGroup();
        level2.getElements().add(level3);
        level3.getElements().add(new StaticHttpUrl("example3", HttpUrl.parse("http://localhost")));

        assertThat(topology.getResourceOptional("example3", HttpUrlResource.class).isPresent(), is(true));

        level1.getElements().remove(level2);

        try {
            topology.getResourceOptional("example", HostAndPortResource.class).get();
            fail("RuntimeException expected");
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage(),
                is("Resource by key 'example' is of type 'class com.ripple.topology.elements.StaticHttpUrl', but was requested as 'interface com.ripple.topology.elements.HostAndPortResource'"));
        }

        try {
            topology.getResource("example", HostAndPortResource.class);
            fail("RuntimeException expected");
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage(),
                is("Resource by key 'example' is of type 'class com.ripple.topology.elements.StaticHttpUrl', but was requested as 'interface com.ripple.topology.elements.HostAndPortResource'"));
        }

        assertThat(topology.getResourceOptional("example2", Resource.class).isPresent(), is(false));
        assertThat(topology.getResourceOptional("example3", Resource.class).isPresent(), is(false));

    }

    @Test
    public void testGetElementsByType() {
        Topology topology = prototype();

        assertThat(topology.getElements(HttpUrlResource.class).size(), is(2));
        assertThat(topology.getElements(HostAndPortResource.class).size(), is(2));
        assertThat(topology.getElements(Resource.class).size(), is(4));
        assertThat(topology.getElements(ElementGroup.class).size(), is(2));
    }

    @Test
    public void testGetElementsByPredicate() {
        Topology topology = prototype();

        assertThat(topology.getElements(HostAndPortResource.class, t -> t.getKey().equals("sf")).size(), is(1));
        assertThat(topology.getElements(HttpUrlResource.class, t -> t.getKey().equals("sf")).size(), is(0));
    }

    @Test
    public void testGlobalVariableSource() {
        Topology topology = new Topology();
        topology.addVariable("item1", "one");
        topology.addElement(new PropertiesVariableSource()
            .addVariable("item1", "uno")
            .addVariable("item2", "dos")
            .addVariable("item3", "tres")
        );
        topology.addElement(new ExampleElement()
            .addProperty("message1", "I will have ${item1} beers")
            .addProperty("message2", "I will have ${item2} tequilas")
            .addProperty("message3", "I will have ${item3} margaritas")
        );

        topology.start().join();
        assertThat(topology.hasFailedDuringStartup(), is(false));

        ExampleElement result = topology.getResource("key", ExampleElement.class);
        assertThat(result.getProperties().getProperty("message1"), is("I will have one beers"));
        assertThat(result.getProperties().getProperty("message2"), is("I will have dos tequilas"));
        assertThat(result.getProperties().getProperty("message3"), is("I will have three margaritas"));
    }

    @Test
    public void testContentLoader() throws IOException {
        Topology topology = new Topology();

        long contentLength = topology.contentLoader().getContent("classpath:/example.properties").contentLength();
        assertThat(contentLength, is(greaterThan(0L)));
    }

    @Test
    public void testShutdownHook() {
        Topology topology = new Topology().registerShutdownHook().addElement(new ExceptingElement());
        topology.startSync();
        assertThat(topology.hasFailedDuringStartup(), is(true));
        logger.info("Shutting down");
        topology.stopSync();
    }

    private Topology prototype() {
        Topology topology = new Topology();
        topology.addElement(new StaticHostAndPort("sf", HostAndPort.fromString("localhost:9090")));
        topology.addElement(new StaticHttpUrl("ny", HttpUrl.parse("http://localhost:8080")));
        SerialElementGroup serialElementGroup = new SerialElementGroup();
        serialElementGroup.addElement(new ParallelElementGroup());
        serialElementGroup.addElement(new StaticHostAndPort("ch", HostAndPort.fromString("localhost:7070")));
        serialElementGroup.addElement(new StaticHttpUrl("la", HttpUrl.parse("http://localhost:6060")));
        topology.addElement(serialElementGroup);
        return topology;
    }

    private static class ExampleElement implements PropertiesAwareResource<ExampleElement>, ScopedVariableSource {

        private Properties properties = new Properties();

        @Override
        public Properties getProperties() {
            return properties;
        }

        @Override
        public String getKey() {
            return "key";
        }

        @Override
        public Map<String, Object> getVariables() {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("item3", "three");
            return map;
        }
    }

    private static class ExceptingElement implements Lifecycle {

        @Override
        public CompletableFuture<Void> start(final Topology topology) {
            throw new RuntimeException();
        }

        @Override
        public CompletableFuture<Void> stop(final Topology topology) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
