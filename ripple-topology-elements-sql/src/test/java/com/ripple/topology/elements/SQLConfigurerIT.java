package com.ripple.topology.elements;

import com.google.common.net.HostAndPort;
import com.ripple.topology.Topology;
import com.ripple.topology.serialization.TopologyMarshaller;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.postgresql.Driver;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class SQLConfigurerIT {

    private TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

    @Test(groups = {"manual"})
    public void testJavaAPI() {
        Topology topology = new Topology()
            .addVariable("currentDate", LocalDateTime.now())
            .addVariable("counter", new Counter())
            .addElement(
                new StaticHostAndPort("db", HostAndPort.fromString("localhost:5432"))
            )
            .addElement(
                new SQLConfigurer(Driver.class, "jdbc:postgresql://${db.hostAndPort}/example", new SQLCredentials("dev", "dev"))
                .addStatement("insert into example_record (name, created_dttm, modified_dttm, version) values ('test', '${currentDate}', '${currentDate}', ${counter})")
                .addStatementfile("/insert1.sql")
                .addStatementfile("/insert2.sql")
            )
            ;

        topology.startSync();
    }

    @Test(enabled = false, groups = {"manual"})
    public void testYamlApi() {

        String yaml = "---\n"
            + "variables:\n"
            + "  currentDate: 2019-02-21T15:32:30.507\n"
            + "  counter: 1\n"
            + "elements:\n"
            + "- type: StaticHostAndPort\n"
            + "  key: db\n"
            + "  hostAndPort: localhost:5432\n"
            + "- type: SQLConfigurer\n"
            + "  driverClass: org.postgresql.Driver\n"
            + "  jdbcUrl: jdbc:postgresql://${db.hostAndPort}/example\n"
            + "  credentials:\n"
            + "    username: dev\n"
            + "    password: dev\n"
            + "  statements:\n"
            + "  - insert into example_record (name, created_dttm, modified_dttm, version) values\n"
            + "    ('test', '${currentDate}', '${currentDate}', ${counter})\n"
            + "  statementFiles:\n"
            + "  - /insert1.sql\n"
            + "  - /insert2.sql\n"
            ;

        marshaller.read(yaml).startSync();
    }

    private static class Counter {
        private AtomicInteger ai = new AtomicInteger(0);

        @Override
        public String toString() {
            return String.valueOf(ai.incrementAndGet());
        }
    }
}
