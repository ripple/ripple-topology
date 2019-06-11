package com.ripple.topology.elements;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.typeCompatibleWith;

import com.google.common.net.HostAndPort;
import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class SQLConfigurerTest {

    private static final Logger logger = LoggerFactory.getLogger(SQLConfigurerTest.class);
    private TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

    @Test
    public void testJavaToYaml() {
        Topology topology = new Topology()
            .addElement(
                new StaticHostAndPort("db", HostAndPort.fromString("localhost:5432"))
            )
            .addElement(
                new SQLConfigurer(Driver.class, "jdbc:postgresql://${db.hostAndPort}/example", new SQLCredentials("user", "password"))
                .addStatement("select * from example")
                .addStatementfile("/inserts.sql")
            )
            ;

        String yaml = marshaller.writeAsString(topology);

        logger.info("{}", yaml);

        final String expected = "---\n"
            + "elements:\n"
            + "- type: StaticHostAndPort\n"
            + "  key: db\n"
            + "  hostAndPort: localhost:5432\n"
            + "- type: SQLConfigurer\n"
            + "  driverClass: org.postgresql.Driver\n"
            + "  jdbcUrl: jdbc:postgresql://${db.hostAndPort}/example\n"
            + "  credentials:\n"
            + "    username: user\n"
            + "    password: password\n"
            + "  statements:\n"
            + "  - select * from example\n"
            + "  statementFiles:\n"
            + "  - /inserts.sql\n"
            ;

        assertThat(yaml, is(expected));
    }

    @Test
    public void testYamlToJava() {
        final String input = "---\n"
            + "elements:\n"
            + "- type: StaticHostAndPort\n"
            + "  key: db\n"
            + "  hostAndPort: localhost:5432\n"
            + "- type: SQLConfigurer\n"
            + "  driverClass: org.postgresql.Driver\n"
            + "  jdbcUrl: jdbc:postgresql://${db.hostAndPort}/example\n"
            + "  credentials:\n"
            + "    username: user\n"
            + "    password: password\n"
            + "  statements:\n"
            + "  - select * from example\n"
            ;


        Topology topology = marshaller.read(input);

        SQLConfigurer element = topology.getElements(SQLConfigurer.class).get(0);
        assertThat(element.getDriverClass(), is(typeCompatibleWith(Driver.class)));
        assertThat(element.getCredentials().getUsername(), is("user"));
        assertThat(element.getCredentials().getPassword(), is("password"));
        assertThat(element.getStatements().size(), is(1));
        assertThat(element.getStatements().get(0), is("select * from example"));
        assertThat(element.getJdbcUrl(), is("jdbc:postgresql://${db.hostAndPort}/example"));

    }
}
