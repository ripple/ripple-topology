package com.ripple.topology.elements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.net.HostAndPort;
import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import java.io.IOException;
import okhttp3.HttpUrl;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class PropertiesConfigurerTest {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesConfigurerTest.class);

    @Test
    public void testVariableResolution() {
        Topology topology = new Topology();
        topology.addVariable("globalVar1", "one");

        PropertiesConfigurer configurer = new PropertiesConfigurer()
            .addVariable("globalVar2", "two, ${globalVar1}")
            .addProperty("prop1", "${globalVar2}")
            .addProperty("prop2", "${globalVar2}");

        topology.addElement(configurer);

        TestResource testResource = new TestResource();
        testResource.addProperty("prop1", "one, two");

        topology.addElement(testResource);

        topology.start().join();

        assertThat(topology.getElements(PropertiesConfigurer.class).get(0).getProperties().getProperty("prop2"),
            is("two, one"));
        assertThat(topology.getElements(TestResource.class).get(0).getProperties().getProperty("prop1"),
            is("one, two"));
        assertThat(topology.getElements(TestResource.class).get(0).getProperties().getProperty("prop2"),
            is("two, one"));
    }

    @Test
    public void testJavaToYaml() {

        Topology topology = new Topology();
        topology.addVariable("context", "xcurrent");

        topology.addElement(new StaticHostAndPort("pg", HostAndPort.fromString("localhost:5432")));
        topology.addElement(new StaticHostAndPort("ms", HostAndPort.fromString("localhost:6789")));
        topology.addElement(new StaticHostAndPort("ldap", HostAndPort.fromString("localhost:6789")));
        topology.addElement(new StaticHttpUrl("broker", HttpUrl.parse("http://localhost:9999")));

        PropertiesConfigurer databaseConfigurer = new PropertiesConfigurer()
            .setApplyToKeyEqualing("application")
            .setApplyToResourcesOfType(TestResource.class)
            .setApplyToKeysEndingWith("ion")
            .setApplyToKeysStartingWith("app")
            .setApplyToKeysContaining("lica")
            .addVariable("pg_endpoint", "${pg.hostAndPort}")
            .addVariable("ms_endpoint", "${ms.hostAndPort}")
            .addVariable("pg_dialect", "postgres")
            .addVariable("ms_dialect", "mssql")
            .addVariable("pg_driver", "postgresql")
            .addVariable("ms_driver", "mssql")
            .addVariable("ms_jdbcUrl", "jdbc:${ms_driver}://${ms_endpoint}/xcurrent")
            .addVariable("pg_jdbcUrl", "jdbc:${pg_driver}://${pg_endpoint}/xcurrent")
            .addVariable("user", "jimmie!")
            .addProperty("db", "${pg_dialect}")
            .addProperty("configurationservice.db.postgres.url", "jdbc:postgresql/${pg.hostAndPort}/xcurrent")
            .addProperty("configurationservice.db.${pg_dialect}.user", "${user}")
            .addProperty("ledgerservice.db.${ms_dialect}.url", "${ms_jdbcUrl}")
            .addProperty("paymentorchestrationservice.db.${pg_dialect}.url", "${pg_jdbcUrl}")
            .addProperty("peerservice.db.${pg_dialect}.url", "${pg_jdbcUrl}")
            .addProperty("quoteservice.db.${pg_dialect}.url", "${pg_jdbcUrl}")
            .addProperty("ripplenetpublickeyservice.db.${pg_dialect}.url", "${pg_jdbcUrl}")
            .addProperty("validator.db.${ms_dialect}.url", "${ms_jdbcUrl}")
            .addProperty("xcurrent.db.${ms_dialect}.url", "${ms_jdbcUrl}")
            .addProperty("xcurrentLiquidity.db.${ms_dialect}.url", "${ms_jdbcUrl}")
            .addProperty("xcurrentLiquidity.ldap.endpoint", "${ldap.hostAndPort}");
        topology.addElement(databaseConfigurer)
        ;

        TestResource application = new TestResource("application")
            .addVariable("path", "${context}/${key}")
            .addProperty("xcurrent.broker.url", "${broker.httpUrl}${path}")
            ;

        topology.addElement(application);
        topology.startSync();

        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

        assertThat(
            topology.getResource("application", TestResource.class).getProperties()
                .getProperty("xcurrentLiquidity.ldap.endpoint"),
            is("localhost:6789")
        );

        logger.info("{}", marshaller.writeAsString(topology));
    }

    @Test
    public void testYamlToJson() throws IOException {
        String input = "---\n"
            + "variables:\n"
            + "  context: \"xcurrent\"\n"
            + "elements:\n"
            + "- type: \"StaticHostAndPort\"\n"
            + "  key: \"pg\"\n"
            + "  hostAndPort: \"localhost:5432\"\n"
            + "- type: \"StaticHostAndPort\"\n"
            + "  key: \"ms\"\n"
            + "  hostAndPort: \"localhost:6789\"\n"
            + "- type: \"StaticHostAndPort\"\n"
            + "  key: \"ldap\"\n"
            + "  hostAndPort: \"localhost:6789\"\n"
            + "- type: \"StaticHttpUrl\"\n"
            + "  key: \"broker\"\n"
            + "  httpUrl: \"http://localhost:9999/\"\n"
            + "- type: \"PropertiesConfigurer\"\n"
            + "  variables:\n"
            + "    pg_endpoint: \"${pg.hostAndPort}\"\n"
            + "    ms_endpoint: \"${ms.hostAndPort}\"\n"
            + "    pg_dialect: \"postgres\"\n"
            + "    ms_dialect: \"mssql\"\n"
            + "    pg_driver: \"postgresql\"\n"
            + "    ms_driver: \"mssql\"\n"
            + "    ms_jdbcUrl: \"jdbc:${ms_driver}://${ms_endpoint}/xcurrent\"\n"
            + "    pg_jdbcUrl: \"jdbc:${pg_driver}://${pg_endpoint}/xcurrent\"\n"
            + "    user: \"jimmie!\"\n"
            + "  properties:\n"
            + "    configurationservice.db.postgres.url: \"jdbc:postgresql/${pg.hostAndPort}/xcurrent\"\n"
            + "    peerservice.db.${pg_dialect}.url: \"${pg_jdbcUrl}\"\n"
            + "    paymentorchestrationservice.db.${pg_dialect}.url: \"${pg_jdbcUrl}\"\n"
            + "    xcurrentLiquidity.ldap.endpoint: \"${ldap.hostAndPort}\"\n"
            + "    ledgerservice.db.${ms_dialect}.url: \"${ms_jdbcUrl}\"\n"
            + "    quoteservice.db.${pg_dialect}.url: \"${pg_jdbcUrl}\"\n"
            + "    validator.db.${ms_dialect}.url: \"${ms_jdbcUrl}\"\n"
            + "    ripplenetpublickeyservice.db.${pg_dialect}.url: \"${pg_jdbcUrl}\"\n"
            + "    configurationservice.db.${pg_dialect}.user: \"${user}\"\n"
            + "    xcurrentLiquidity.db.${ms_dialect}.url: \"${ms_jdbcUrl}\"\n"
            + "    db: \"${pg_dialect}\"\n"
            + "    xcurrent.db.${ms_dialect}.url: \"${ms_jdbcUrl}\"\n"
            + "  applyToKeyEqualing: \"application\"\n"
            + "  applyToKeysStartingWith: \"app\"\n"
            + "  applyToKeysContaining: \"lica\"\n"
            + "  applyToKeysEndingWith: \"ion\"\n"
            + "  applyToResourcesOfType: \"com.ripple.topology.elements.PropertiesConfigurerTest$TestResource\"\n"
            + "- type: \"test-resource\"\n"
            + "  key: \"application\"\n"
            + "  properties:\n"
            + "    configurationservice.db.postgres.url: \"jdbc:postgresql/localhost:5432/xcurrent\"\n"
            + "    peerservice.db.${pg_dialect}.url: \"${pg_jdbcUrl}\"\n"
            + "    paymentorchestrationservice.db.${pg_dialect}.url: \"${pg_jdbcUrl}\"\n"
            + "    xcurrentLiquidity.ldap.endpoint: \"localhost:6789\"\n"
            + "    ledgerservice.db.${ms_dialect}.url: \"${ms_jdbcUrl}\"\n"
            + "    quoteservice.db.${pg_dialect}.url: \"${pg_jdbcUrl}\"\n"
            + "    validator.db.${ms_dialect}.url: \"${ms_jdbcUrl}\"\n"
            + "    ripplenetpublickeyservice.db.${pg_dialect}.url: \"${pg_jdbcUrl}\"\n"
            + "    configurationservice.db.${pg_dialect}.user: \"${user}\"\n"
            + "    xcurrent.broker.url: \"http://localhost:9999/xcurrent/application\"\n"
            + "    xcurrentLiquidity.db.${ms_dialect}.url: \"${ms_jdbcUrl}\"\n"
            + "    db: \"${pg_dialect}\"\n"
            + "    xcurrent.db.${ms_dialect}.url: \"${ms_jdbcUrl}\"\n"
            + "  variables:\n"
            + "    path: \"${context}/${key}\"\n";

        Topology topology = TopologyMarshaller.forYaml().read(input);

        assertThat(topology.getElements(PropertiesConfigurer.class).get(0).getApplyToResourcesOfType(),
            Matchers.typeCompatibleWith(TestResource.class));
    }

    @JsonTypeName("test-resource")
    public static class TestResource extends AbstractPropertiesAwareResource<TestResource> {

        public TestResource(final String key) {
            super(key);
        }

        public TestResource() {
            super("");
        }
    }
}
