package com.ripple.topology;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashicorp.nomad.apimodel.Job;
import com.ripple.topology.elements.EnvironmentConfigurer;
import com.ripple.topology.elements.PropertiesConfigurer;
import com.ripple.topology.serialization.TopologyMarshaller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class NomadJobTest {

    private static final Logger logger = LoggerFactory.getLogger(NomadJobTest.class);

    @Test(enabled = false)
    public void testStartStopJob() throws Exception {

        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();
        Topology topology = marshaller.readFromClasspath("/topology-sf-ny.yaml");

        topology.addVariable("database_id", UUID.randomUUID().toString());
        topology.addVariable("application_id", UUID.randomUUID().toString());

        topology.start().join();
    }

    @Test(enabled = false)
    public void testStartXRapid() throws Exception {
        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();
        Topology topology = marshaller.readFromClasspath("/topology-xrapid.yaml");
        topology.start().join();
    }

    @Test
    public void testSerialize() throws Exception {
        Topology topology = new Topology();

        NomadJob nomadGroup = new NomadJob();
        nomadGroup.setClusterManager(HttpUrl.parse("http://localhost:1234"));

        nomadGroup.setTaskHealthTimeoutMillis(120 * 1000);
        nomadGroup.setTaskHealthCheckPauseMillis(1000);

        nomadGroup.setGroupId("test-application");
        nomadGroup.setVaultToken("vault-token");
        nomadGroup.getElements().add(
            new NomadTask("sf", "artifactory.ops.ripple"
                + ".com:6555/ripplenet/applications/xcurrent:0.25-SNAPSHOT", 9000)
                .addArtifact("https://artifactory.ops.ripple"
                    + ".com/artifactory/ripplenet-topology/sf-logback.xml", "etc/logback.xml")
                .addArtifact("https://artifactory.ops.ripple"
                    + ".com/artifactory/ripplenet-topology/sf-rn-keystore.pkcs12", "etc/rn-keystore.pkcs12")
                .addArtifact("https://artifactory.ops.ripple"
                        + ".com/artifactory/ripplenet-topology/sf-usage-keystore.p12",
                    "etc/usage-keystore.p12")
                .addArtifact("https://artifactory.ops.ripple"
                        + ".com/artifactory/ripplenet-topology/sf-xcurrent.properties.tpl",
                    "local/xcurrent.properties.tpl")
        );

        topology.addElement(nomadGroup);

        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

        String yaml = marshaller.writeAsString(topology);

        String expected = "---\n"
            + "elements:\n"
            + "- type: NomadJob\n"
            + "  clusterManager: http://localhost:1234/\n"
            + "  region: global\n"
            + "  datacenter: dc1\n"
            + "  groupId: test-application\n"
            + "  vaultToken: vault-token\n"
            + "  elements:\n"
            + "  - type: NomadTask\n"
            + "    key: sf\n"
            + "    image: artifactory.ops.ripple.com:6555/ripplenet/applications/xcurrent:0.25-SNAPSHOT\n"
            + "    port: 9000\n"
            + "    maxCpu: 256\n"
            + "    maxRam: 64\n"
            + "    artifacts:\n"
            + "    - source: https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-logback.xml\n"
            + "      destination: etc/logback.xml\n"
            + "    - source: https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-rn-keystore.pkcs12\n"
            + "      destination: etc/rn-keystore.pkcs12\n"
            + "    - source: https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-usage-keystore.p12\n"
            + "      destination: etc/usage-keystore.p12\n"
            + "    - source: https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-xcurrent.properties.tpl\n"
            + "      destination: local/xcurrent.properties.tpl\n"
            + "  taskHealthTimeoutMillis: 120000\n"
            + "  taskHealthCheckPauseMillis: 1000\n";

        logger.info("{}", yaml);
        assertThat(yaml, is(expected));
    }

    @Test
    public void testDeserializeJobWithTimeouts() {
        String input = "---\n"
            + "elements:\n"
            + "- type: NomadJob\n"
            + "  clusterManager: http://localhost:1234/\n"
            + "  region: global\n"
            + "  datacenter: dc1\n"
            + "  groupId: test-application\n"
            + "  vaultToken: vault-token\n"
            + "  elements:\n"
            + "  - type: NomadTask\n"
            + "    key: sf\n"
            + "    image: artifactory.ops.ripple.com:6555/ripplenet/applications/xcurrent:0.25-SNAPSHOT\n"
            + "    port: 9000\n"
            + "    maxCpu: 256\n"
            + "    maxRam: 64\n"
            + "    artifacts:\n"
            + "    - source: https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-logback.xml\n"
            + "      destination: etc/logback.xml\n"
            + "    - source: https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-rn-keystore.pkcs12\n"
            + "      destination: etc/rn-keystore.pkcs12\n"
            + "    - source: https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-usage-keystore.p12\n"
            + "      destination: etc/usage-keystore.p12\n"
            + "    - source: https://artifactory.ops.ripple.com/artifactory/ripplenet-topology/sf-xcurrent.properties.tpl\n"
            + "      destination: local/xcurrent.properties.tpl\n"
            + "  taskHealthTimeoutMillis: 110000\n"
            + "  taskHealthCheckPauseMillis: 2000\n";

        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();

        Topology topology = marshaller.read(input);
        NomadJob job = topology.getElements(NomadJob.class).get(0);
        assertThat(job.getTaskHealthTimeoutMillis(), is(110 * 1000));
        assertThat(job.getTaskHealthCheckPauseMillis(), is(2000));
    }

    @Test
    public void testDeserialize() throws IOException {

        TopologyMarshaller marshaller = TopologyMarshaller.forYaml();
        Topology topology = marshaller.readFromClasspath("/topology-sf-ny.yaml");
        assertThat(topology.getResourceOptional("ny", NomadTask.class).isPresent(), is(true));
        assertThat(topology.getResourceOptional("sf", NomadTask.class).isPresent(), is(true));
        assertThat(topology.getResourceOptional("sf-db", NomadTask.class).isPresent(), is(true));
        assertThat(topology.getResourceOptional("ny-db", NomadTask.class).isPresent(), is(true));
        assertThat(topology.getElements().get(0), is(instanceOf(EnvironmentConfigurer.class)));
        assertThat(topology.getElements().get(1), is(instanceOf(EnvironmentConfigurer.class)));
        assertThat(topology.getElements().get(3), is(instanceOf(PropertiesConfigurer.class)));
        assertThat(topology.getElements().get(4), is(instanceOf(EnvironmentConfigurer.class)));
        assertThat(topology.getElements().get(5), is(instanceOf(PropertiesConfigurer.class)));
        assertThat(topology.getElements().get(6), is(instanceOf(EnvironmentConfigurer.class)));
    }

    @Test
    public void testCreateNomadDatabaseJob() throws Exception {
        NomadJob nomadGroup = new NomadJob();
        nomadGroup.setClusterManager(HttpUrl.parse("http://localhost:1234"));
        nomadGroup.setGroupId("test-application");
        nomadGroup.setVaultToken("test-vault-token");
        nomadGroup.setDatacenter("standalone");
        nomadGroup.getElements().add(
            new NomadTask("sf-db", "artifactory.ops.ripple.com:6555/ripplenet/databases/postgres-9.4", 5432)
        );
        nomadGroup.getElements().add(
            new NomadTask("ny-db", "artifactory.ops.ripple.com:6555/ripplenet/databases/mssql-2017", 1433)
        );

        final Job job = nomadGroup.createJob();

        final String expectedJob = "{\n"
            + "  \"Stop\":null,\n"
            + "  \"Region\":\"global\",\n"
            + "  \"Namespace\":null,\n"
            + "  \"ID\":\"test-application\",\n"
            + "  \"ParentID\":null,\n"
            + "  \"Name\":\"test-application\",\n"
            + "  \"Type\":\"service\",\n"
            + "  \"Priority\":null,\n"
            + "  \"AllAtOnce\":null,\n"
            + "  \"Datacenters\":[\n"
            + "    \"standalone\"\n"
            + "  ],\n"
            + "  \"Constraints\":null,\n"
            + "  \"TaskGroups\":[\n"
            + "    {\n"
            + "      \"Name\":\"sf-db\",\n"
            + "      \"Count\":null,\n"
            + "      \"Constraints\":null,\n"
            + "      \"Tasks\":[\n"
            + "        {\n"
            + "          \"Name\":\"sf-db\",\n"
            + "          \"Driver\":\"docker\",\n"
            + "          \"User\":null,\n"
            + "          \"Config\":{\n"
            + "            \"image\":\"artifactory.ops.ripple.com:6555/ripplenet/databases/postgres-9.4\",\n"
            + "            \"port_map\":[\n"
            + "              {\n"
            + "                \"sf-db\":5432\n"
            + "              }\n"
            + "            ]\n"
            + "          },\n"
            + "          \"Constraints\":null,\n"
            + "          \"Env\":{\"JAVA_MEM\":\"-Xmx64m\"},\n"
            + "          \"Services\":[\n"
            + "            {\n"
            + "              \"Id\":null,\n"
            + "              \"Name\":\"sf-db\",\n"
            + "              \"Tags\":null,\n"
            + "              \"CanaryTags\":null,\n"
            + "              \"PortLabel\":\"sf-db\",\n"
            + "              \"AddressMode\":null,\n"
            + "              \"Checks\":[\n"
            + "                {\n"
            + "                  \"Id\":null,\n"
            + "                  \"Name\":\"sf-db-tcp-check\",\n"
            + "                  \"Type\":\"tcp\",\n"
            + "                  \"Command\":null,\n"
            + "                  \"Args\":null,\n"
            + "                  \"Path\":null,\n"
            + "                  \"Protocol\":null,\n"
            + "                  \"PortLabel\":\"sf-db\",\n"
            + "                  \"AddressMode\":null,\n"
            + "                  \"Interval\":100000000000,\n"
            + "                  \"Timeout\":2000000000,\n"
            + "                  \"InitialStatus\":null,\n"
            + "                  \"TLSSkipVerify\":false,\n"
            + "                  \"Header\":null,\n"
            + "                  \"Method\":null,\n"
            + "                  \"CheckRestart\":null,\n"
            + "                  \"GRPCService\":null,\n"
            + "                  \"GRPCUseTLS\":false\n"
            + "                }\n"
            + "              ],\n"
            + "              \"CheckRestart\":null\n"
            + "            }\n"
            + "          ],\n"
            + "          \"Resources\":{\n"
            + "            \"CPU\":256,\n"
            + "            \"MemoryMB\":64,\n"
            + "            \"DiskMB\":null,\n"
            + "            \"IOPS\":null,\n"
            + "            \"Networks\":[\n"
            + "              {\n"
            + "                \"Device\":null,\n"
            + "                \"CIDR\":null,\n"
            + "                \"IP\":null,\n"
            + "                \"ReservedPorts\":null,\n"
            + "                \"DynamicPorts\":[\n"
            + "                  {\n"
            + "                    \"Label\":\"sf-db\",\n"
            + "                    \"Value\":0\n"
            + "                  }\n"
            + "                ],\n"
            + "                \"MBits\":null\n"
            + "              }\n"
            + "            ]\n"
            + "          },\n"
            + "          \"Meta\":null,\n"
            + "          \"KillTimeout\":null,\n"
            + "          \"LogConfig\":null,\n"
            + "          \"Artifacts\":null,\n"
            + "          \"Vault\":null,\n"
            + "          \"Templates\":null,\n"
            + "          \"DispatchPayload\":null,\n"
            + "          \"Leader\":false,\n"
            + "          \"ShutdownDelay\":0,\n"
            + "          \"KillSignal\":null\n"
            + "        }\n"
            + "      ],\n"
            + "      \"RestartPolicy\":null,\n"
            + "      \"ReschedulePolicy\":null,\n"
            + "      \"EphemeralDisk\":null,\n"
            + "      \"Update\":null,\n"
            + "      \"Migrate\":null,\n"
            + "      \"Meta\":null\n"
            + "    },\n"
            + "    {\n"
            + "      \"Name\":\"ny-db\",\n"
            + "      \"Count\":null,\n"
            + "      \"Constraints\":null,\n"
            + "      \"Tasks\":[\n"
            + "        {\n"
            + "          \"Name\":\"ny-db\",\n"
            + "          \"Driver\":\"docker\",\n"
            + "          \"User\":null,\n"
            + "          \"Config\":{\n"
            + "            \"image\":\"artifactory.ops.ripple.com:6555/ripplenet/databases/mssql-2017\",\n"
            + "            \"port_map\":[\n"
            + "              {\n"
            + "                \"ny-db\":1433\n"
            + "              }\n"
            + "            ]\n"
            + "          },\n"
            + "          \"Constraints\":null,\n"
            + "          \"Env\":{\"JAVA_MEM\":\"-Xmx64m\"},\n"
            + "          \"Services\":[\n"
            + "            {\n"
            + "              \"Id\":null,\n"
            + "              \"Name\":\"ny-db\",\n"
            + "              \"Tags\":null,\n"
            + "              \"CanaryTags\":null,\n"
            + "              \"PortLabel\":\"ny-db\",\n"
            + "              \"AddressMode\":null,\n"
            + "              \"Checks\":[\n"
            + "                {\n"
            + "                  \"Id\":null,\n"
            + "                  \"Name\":\"ny-db-tcp-check\",\n"
            + "                  \"Type\":\"tcp\",\n"
            + "                  \"Command\":null,\n"
            + "                  \"Args\":null,\n"
            + "                  \"Path\":null,\n"
            + "                  \"Protocol\":null,\n"
            + "                  \"PortLabel\":\"ny-db\",\n"
            + "                  \"AddressMode\":null,\n"
            + "                  \"Interval\":100000000000,\n"
            + "                  \"Timeout\":2000000000,\n"
            + "                  \"InitialStatus\":null,\n"
            + "                  \"TLSSkipVerify\":false,\n"
            + "                  \"Header\":null,\n"
            + "                  \"Method\":null,\n"
            + "                  \"CheckRestart\":null,\n"
            + "                  \"GRPCService\":null,\n"
            + "                  \"GRPCUseTLS\":false\n"
            + "                }\n"
            + "              ],\n"
            + "              \"CheckRestart\":null\n"
            + "            }\n"
            + "          ],\n"
            + "          \"Resources\":{\n"
            + "            \"CPU\":256,\n"
            + "            \"MemoryMB\":64,\n"
            + "            \"DiskMB\":null,\n"
            + "            \"IOPS\":null,\n"
            + "            \"Networks\":[\n"
            + "              {\n"
            + "                \"Device\":null,\n"
            + "                \"CIDR\":null,\n"
            + "                \"IP\":null,\n"
            + "                \"ReservedPorts\":null,\n"
            + "                \"DynamicPorts\":[\n"
            + "                  {\n"
            + "                    \"Label\":\"ny-db\",\n"
            + "                    \"Value\":0\n"
            + "                  }\n"
            + "                ],\n"
            + "                \"MBits\":null\n"
            + "              }\n"
            + "            ]\n"
            + "          },\n"
            + "          \"Meta\":null,\n"
            + "          \"KillTimeout\":null,\n"
            + "          \"LogConfig\":null,\n"
            + "          \"Artifacts\":null,\n"
            + "          \"Vault\":null,\n"
            + "          \"Templates\":null,\n"
            + "          \"DispatchPayload\":null,\n"
            + "          \"Leader\":false,\n"
            + "          \"ShutdownDelay\":0,\n"
            + "          \"KillSignal\":null\n"
            + "        }\n"
            + "      ],\n"
            + "      \"RestartPolicy\":null,\n"
            + "      \"ReschedulePolicy\":null,\n"
            + "      \"EphemeralDisk\":null,\n"
            + "      \"Update\":null,\n"
            + "      \"Migrate\":null,\n"
            + "      \"Meta\":null\n"
            + "    }\n"
            + "  ],\n"
            + "  \"Update\":null,\n"
            + "  \"Periodic\":null,\n"
            + "  \"ParameterizedJob\":null,\n"
            + "  \"Dispatched\":false,\n"
            + "  \"Payload\":null,\n"
            + "  \"Reschedule\":null,\n"
            + "  \"Migrate\":null,\n"
            + "  \"Meta\":null,\n"
            + "  \"VaultToken\":\"vault-token\",\n"
            + "  \"Status\":null,\n"
            + "  \"StatusDescription\":null,\n"
            + "  \"Stable\":null,\n"
            + "  \"Version\":null,\n"
            + "  \"SubmitTime\":null,\n"
            + "  \"CreateIndex\":null,\n"
            + "  \"ModifyIndex\":null,\n"
            + "  \"JobModifyIndex\":null\n"
            + "}";

        ObjectMapper mapper = new ObjectMapper();
        final JsonNode actualJsonNode = mapper.readTree(job.toString());
        final JsonNode expectedJsonNode = mapper.readTree(expectedJob);

        assertThat(actualJsonNode, is(expectedJsonNode));
    }

    @Test
    public void testCreateApplicationJob() throws Exception {
        NomadJob nomadGroup = new NomadJob();
        nomadGroup.setClusterManager(HttpUrl.parse("http://fake-server:1234"));
        nomadGroup.setGroupId("test-application");
        nomadGroup.setVaultToken("test-vault-token");
        nomadGroup.setDatacenter("standalone");

        final NomadTask postgresApplication = new NomadTask("sf",
            "artifactory.ops.ripple"
                + ".com:6555/ripplenet/applications/xcurrent:0.25-SNAPSHOT", 9000);

        postgresApplication.addVolume("etc", "/application/etc");

        postgresApplication.addArtifact("https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-logback.xml", "etc/logback.xml")
            .addArtifact("https://artifactory.ops.ripple"
                + ".com/artifactory/ripplenet-topology/sf-rn-keystore.pkcs12", "etc/rn-keystore.pkcs12")
            .addArtifact("https://artifactory.ops.ripple"
                    + ".com/artifactory/ripplenet-topology/sf-usage-keystore.p12",
                "etc/usage-keystore.p12")
            .addArtifact("https://artifactory.ops.ripple"
                    + ".com/artifactory/ripplenet-topology/sf-xcurrent.properties.tpl",
                "local/xcurrent.properties.tpl")
            .addTemplate("local/xcurrent.properties.tpl", "etc/xcurrent.properties")
            .addTag("standalone.enable=true")
            .addTag("standalone.frontend.entryPoints=http,https")
            .addTag("standalone.frontend.rule=Host:${key}.usw2.testdev.ripple.com")
            .setHttpCheck("/")
        ;
        nomadGroup.getElements().add(postgresApplication);

        final NomadTask mssqlApplication = new NomadTask("ny",
            "artifactory.ops.ripple"
                + ".com:6555/ripplenet/applications/xcurrent:0.22.0", 9000);

        mssqlApplication.addVolume("etc", "/application/etc");
        mssqlApplication.addArtifact("https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/ny-logback.xml", "etc/logback.xml")
            .addArtifact("https://artifactory.ops.ripple"
                + ".com/artifactory/ripplenet-topology/ny-rn-keystore.pkcs12", "etc/rn-keystore.pkcs12")
            .addArtifact("https://artifactory.ops.ripple"
                + ".com/artifactory/ripplenet-topology/ny-usage-keystore.p12", "etc/usage-keystore.p12")
            .addArtifact("https://artifactory.ops.ripple"
                + ".com/artifactory/ripplenet-topology/ny-xcurrent.properties.tpl", "local/xcurrent.properties.tpl")
            .addTemplate("local/xcurrent.properties.tpl", "etc/xcurrent.properties")
            .addTag("standalone.enable=true")
            .addTag("standalone.frontend.entryPoints=http,https")
            .addTag("standalone.frontend.rule=Host:${key}.usw2.testdev.ripple.com")
            .setHttpCheck("/")
        ;

        nomadGroup.getElements().add(mssqlApplication);

        final Job job = nomadGroup.createJob();

        final String expectedJob = "{\n"
            + "  \"Stop\":null,\n"
            + "  \"Region\":\"global\",\n"
            + "  \"Namespace\":null,\n"
            + "  \"ID\":\"test-application\",\n"
            + "  \"ParentID\":null,\n"
            + "  \"Name\":\"test-application\",\n"
            + "  \"Type\":\"service\",\n"
            + "  \"Priority\":null,\n"
            + "  \"AllAtOnce\":null,\n"
            + "  \"Datacenters\":[\n"
            + "    \"standalone\"\n"
            + "  ],\n"
            + "  \"Constraints\":null,\n"
            + "  \"TaskGroups\":[\n"
            + "    {\n"
            + "      \"Name\":\"sf\",\n"
            + "      \"Count\":null,\n"
            + "      \"Constraints\":null,\n"
            + "      \"Tasks\":[\n"
            + "        {\n"
            + "          \"Name\":\"sf\",\n"
            + "          \"Driver\":\"docker\",\n"
            + "          \"User\":null,\n"
            + "          \"Config\":{\n"
            + "            \"image\":\"artifactory.ops.ripple"
            + ".com:6555/ripplenet/applications/xcurrent:0.25-SNAPSHOT\",\n"
            + "            \"port_map\":[\n"
            + "              {\n"
            + "                \"sf\":9000\n"
            + "              }\n"
            + "            ],\n"
            + "            \"volumes\":[\n"
            + "              \"etc:/application/etc\"\n"
            + "            ],\n"
            + "            \"force_pull\": true\n"
            + "          },\n"
            + "          \"Constraints\":null,\n"
            + "          \"Env\":{\"JAVA_MEM\":\"-Xmx64m\"},\n"
            + "          \"Services\":[\n"
            + "            {\n"
            + "              \"Id\":null,\n"
            + "              \"Name\":\"sf\",\n"
            + "              \"Tags\":[\n"
            + "                \"standalone.enable=true\",\n"
            + "                \"standalone.frontend.entryPoints=http,https\",\n"
            + "                \"standalone.frontend.rule=Host:${key}.usw2.testdev.ripple.com\"\n"
            + "              ],\n"
            + "              \"CanaryTags\":null,\n"
            + "              \"PortLabel\":\"sf\",\n"
            + "              \"AddressMode\":null,\n"
            + "              \"Checks\":[\n"
            + "                {\n"
            + "                  \"Id\":null,\n"
            + "                  \"Name\":\"sf-tcp-check\",\n"
            + "                  \"Type\":\"tcp\",\n"
            + "                  \"Command\":null,\n"
            + "                  \"Args\":null,\n"
            + "                  \"Path\":null,\n"
            + "                  \"Protocol\":null,\n"
            + "                  \"PortLabel\":\"sf\",\n"
            + "                  \"AddressMode\":null,\n"
            + "                  \"Interval\":100000000000,\n"
            + "                  \"Timeout\":2000000000,\n"
            + "                  \"InitialStatus\":null,\n"
            + "                  \"TLSSkipVerify\":false,\n"
            + "                  \"Header\":null,\n"
            + "                  \"Method\":null,\n"
            + "                  \"CheckRestart\":null,\n"
            + "                  \"GRPCService\":null,\n"
            + "                  \"GRPCUseTLS\":false\n"
            + "                },\n"
            + "                {\n"
            + "                  \"Id\":null,\n"
            + "                  \"Name\":\"sf-http-check\",\n"
            + "                  \"Type\":\"http\",\n"
            + "                  \"Command\":null,\n"
            + "                  \"Args\":null,\n"
            + "                  \"Path\":\"/\",\n"
            + "                  \"Protocol\":null,\n"
            + "                  \"PortLabel\":\"sf\",\n"
            + "                  \"AddressMode\":null,\n"
            + "                  \"Interval\":100000000000,\n"
            + "                  \"Timeout\":2000000000,\n"
            + "                  \"InitialStatus\":null,\n"
            + "                  \"TLSSkipVerify\":false,\n"
            + "                  \"Header\":null,\n"
            + "                  \"Method\":null,\n"
            + "                  \"CheckRestart\":null,\n"
            + "                  \"GRPCService\":null,\n"
            + "                  \"GRPCUseTLS\":false\n"
            + "                }\n"
            + "              ],\n"
            + "              \"CheckRestart\":null\n"
            + "            }\n"
            + "          ],\n"
            + "          \"Resources\":{\n"
            + "            \"CPU\":256,\n"
            + "            \"MemoryMB\":64,\n"
            + "            \"DiskMB\":null,\n"
            + "            \"IOPS\":null,\n"
            + "            \"Networks\":[\n"
            + "              {\n"
            + "                \"Device\":null,\n"
            + "                \"CIDR\":null,\n"
            + "                \"IP\":null,\n"
            + "                \"ReservedPorts\":null,\n"
            + "                \"DynamicPorts\":[\n"
            + "                  {\n"
            + "                    \"Label\":\"sf\",\n"
            + "                    \"Value\":0\n"
            + "                  }\n"
            + "                ],\n"
            + "                \"MBits\":null\n"
            + "              }\n"
            + "            ]\n"
            + "          },\n"
            + "          \"Meta\":null,\n"
            + "          \"KillTimeout\":null,\n"
            + "          \"LogConfig\":null,\n"
            + "          \"Artifacts\":[\n"
            + "            {\n"
            + "              \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-logback.xml\",\n"
            + "              \"GetterOptions\":null,\n"
            + "              \"GetterMode\":\"any\",\n"
            + "              \"RelativeDest\":\"etc/logback.xml\"\n"
            + "            },\n"
            + "            {\n"
            + "              \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-rn-keystore.pkcs12\",\n"
            + "              \"GetterOptions\":null,\n"
            + "              \"GetterMode\":\"any\",\n"
            + "              \"RelativeDest\":\"etc/rn-keystore.pkcs12\"\n"
            + "            },\n"
            + "            {\n"
            + "              \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-usage-keystore.p12\",\n"
            + "              \"GetterOptions\":null,\n"
            + "              \"GetterMode\":\"any\",\n"
            + "              \"RelativeDest\":\"etc/usage-keystore.p12\"\n"
            + "            },\n"
            + "            {\n"
            + "              \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-xcurrent.properties.tpl\",\n"
            + "              \"GetterOptions\":null,\n"
            + "              \"GetterMode\":\"any\",\n"
            + "              \"RelativeDest\":\"local/xcurrent.properties.tpl\"\n"
            + "            }\n"
            + "          ],\n"
            + "          \"Vault\":null,\n"
            + "          \"Templates\":[\n"
            + "            {\n"
            + "              \"SourcePath\":\"local/xcurrent.properties.tpl\",\n"
            + "              \"DestPath\":\"etc/xcurrent.properties\",\n"
            + "              \"EmbeddedTmpl\":null,\n"
            + "              \"ChangeMode\":null,\n"
            + "              \"ChangeSignal\":null,\n"
            + "              \"Splay\":null,\n"
            + "              \"Perms\":null,\n"
            + "              \"LeftDelim\":null,\n"
            + "              \"RightDelim\":null,\n"
            + "              \"Envvars\":null,\n"
            + "              \"VaultGrace\":null\n"
            + "            }\n"
            + "          ],\n"
            + "          \"DispatchPayload\":null,\n"
            + "          \"Leader\":false,\n"
            + "          \"ShutdownDelay\":0,\n"
            + "          \"KillSignal\":null\n"
            + "        }\n"
            + "      ],\n"
            + "      \"RestartPolicy\":null,\n"
            + "      \"ReschedulePolicy\":null,\n"
            + "      \"EphemeralDisk\":null,\n"
            + "      \"Update\":null,\n"
            + "      \"Migrate\":null,\n"
            + "      \"Meta\":null\n"
            + "    },\n"
            + "    {\n"
            + "      \"Name\":\"ny\",\n"
            + "      \"Count\":null,\n"
            + "      \"Constraints\":null,\n"
            + "      \"Tasks\":[\n"
            + "        {\n"
            + "          \"Name\":\"ny\",\n"
            + "          \"Driver\":\"docker\",\n"
            + "          \"User\":null,\n"
            + "          \"Config\":{\n"
            + "            \"image\":\"artifactory.ops.ripple"
            + ".com:6555/ripplenet/applications/xcurrent:0.22.0\",\n"
            + "            \"port_map\":[\n"
            + "              {\n"
            + "                \"ny\":9000\n"
            + "              }\n"
            + "            ],\n"
            + "            \"volumes\":[\n"
            + "              \"etc:/application/etc\"\n"
            + "            ]\n"
            + "          },\n"
            + "          \"Constraints\":null,\n"
            + "          \"Env\":{\"JAVA_MEM\":\"-Xmx64m\"},\n"
            + "          \"Services\":[\n"
            + "            {\n"
            + "              \"Id\":null,\n"
            + "              \"Name\":\"ny\",\n"
            + "              \"Tags\":[\n"
            + "                \"standalone.enable=true\",\n"
            + "                \"standalone.frontend.entryPoints=http,https\",\n"
            + "                \"standalone.frontend.rule=Host:${key}.usw2.testdev.ripple.com\"\n"
            + "              ],\n"
            + "              \"CanaryTags\":null,\n"
            + "              \"PortLabel\":\"ny\",\n"
            + "              \"AddressMode\":null,\n"
            + "              \"Checks\":[\n"
            + "                {\n"
            + "                  \"Id\":null,\n"
            + "                  \"Name\":\"ny-tcp-check\",\n"
            + "                  \"Type\":\"tcp\",\n"
            + "                  \"Command\":null,\n"
            + "                  \"Args\":null,\n"
            + "                  \"Path\":null,\n"
            + "                  \"Protocol\":null,\n"
            + "                  \"PortLabel\":\"ny\",\n"
            + "                  \"AddressMode\":null,\n"
            + "                  \"Interval\":100000000000,\n"
            + "                  \"Timeout\":2000000000,\n"
            + "                  \"InitialStatus\":null,\n"
            + "                  \"TLSSkipVerify\":false,\n"
            + "                  \"Header\":null,\n"
            + "                  \"Method\":null,\n"
            + "                  \"CheckRestart\":null,\n"
            + "                  \"GRPCService\":null,\n"
            + "                  \"GRPCUseTLS\":false\n"
            + "                },\n"
            + "                {\n"
            + "                  \"Id\":null,\n"
            + "                  \"Name\":\"ny-http-check\",\n"
            + "                  \"Type\":\"http\",\n"
            + "                  \"Command\":null,\n"
            + "                  \"Args\":null,\n"
            + "                  \"Path\":\"/\",\n"
            + "                  \"Protocol\":null,\n"
            + "                  \"PortLabel\":\"ny\",\n"
            + "                  \"AddressMode\":null,\n"
            + "                  \"Interval\":100000000000,\n"
            + "                  \"Timeout\":2000000000,\n"
            + "                  \"InitialStatus\":null,\n"
            + "                  \"TLSSkipVerify\":false,\n"
            + "                  \"Header\":null,\n"
            + "                  \"Method\":null,\n"
            + "                  \"CheckRestart\":null,\n"
            + "                  \"GRPCService\":null,\n"
            + "                  \"GRPCUseTLS\":false\n"
            + "                }\n"
            + "              ],\n"
            + "              \"CheckRestart\":null\n"
            + "            }\n"
            + "          ],\n"
            + "          \"Resources\":{\n"
            + "            \"CPU\":256,\n"
            + "            \"MemoryMB\":64,\n"
            + "            \"DiskMB\":null,\n"
            + "            \"IOPS\":null,\n"
            + "            \"Networks\":[\n"
            + "              {\n"
            + "                \"Device\":null,\n"
            + "                \"CIDR\":null,\n"
            + "                \"IP\":null,\n"
            + "                \"ReservedPorts\":null,\n"
            + "                \"DynamicPorts\":[\n"
            + "                  {\n"
            + "                    \"Label\":\"ny\",\n"
            + "                    \"Value\":0\n"
            + "                  }\n"
            + "                ],\n"
            + "                \"MBits\":null\n"
            + "              }\n"
            + "            ]\n"
            + "          },\n"
            + "          \"Meta\":null,\n"
            + "          \"KillTimeout\":null,\n"
            + "          \"LogConfig\":null,\n"
            + "          \"Artifacts\":[\n"
            + "            {\n"
            + "              \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/ny-logback.xml\",\n"
            + "              \"GetterOptions\":null,\n"
            + "              \"GetterMode\":\"any\",\n"
            + "              \"RelativeDest\":\"etc/logback.xml\"\n"
            + "            },\n"
            + "            {\n"
            + "              \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/ny-rn-keystore.pkcs12\",\n"
            + "              \"GetterOptions\":null,\n"
            + "              \"GetterMode\":\"any\",\n"
            + "              \"RelativeDest\":\"etc/rn-keystore.pkcs12\"\n"
            + "            },\n"
            + "            {\n"
            + "              \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/ny-usage-keystore.p12\",\n"
            + "              \"GetterOptions\":null,\n"
            + "              \"GetterMode\":\"any\",\n"
            + "              \"RelativeDest\":\"etc/usage-keystore.p12\"\n"
            + "            },\n"
            + "            {\n"
            + "              \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/ny-xcurrent.properties.tpl\",\n"
            + "              \"GetterOptions\":null,\n"
            + "              \"GetterMode\":\"any\",\n"
            + "              \"RelativeDest\":\"local/xcurrent.properties.tpl\"\n"
            + "            }\n"
            + "          ],\n"
            + "          \"Vault\":null,\n"
            + "          \"Templates\":[\n"
            + "            {\n"
            + "              \"SourcePath\":\"local/xcurrent.properties.tpl\",\n"
            + "              \"DestPath\":\"etc/xcurrent.properties\",\n"
            + "              \"EmbeddedTmpl\":null,\n"
            + "              \"ChangeMode\":null,\n"
            + "              \"ChangeSignal\":null,\n"
            + "              \"Splay\":null,\n"
            + "              \"Perms\":null,\n"
            + "              \"LeftDelim\":null,\n"
            + "              \"RightDelim\":null,\n"
            + "              \"Envvars\":null,\n"
            + "              \"VaultGrace\":null\n"
            + "            }\n"
            + "          ],\n"
            + "          \"DispatchPayload\":null,\n"
            + "          \"Leader\":false,\n"
            + "          \"ShutdownDelay\":0,\n"
            + "          \"KillSignal\":null\n"
            + "        }\n"
            + "      ],\n"
            + "      \"RestartPolicy\":null,\n"
            + "      \"ReschedulePolicy\":null,\n"
            + "      \"EphemeralDisk\":null,\n"
            + "      \"Update\":null,\n"
            + "      \"Migrate\":null,\n"
            + "      \"Meta\":null\n"
            + "    }\n"
            + "  ],\n"
            + "  \"Update\":null,\n"
            + "  \"Periodic\":null,\n"
            + "  \"ParameterizedJob\":null,\n"
            + "  \"Dispatched\":false,\n"
            + "  \"Payload\":null,\n"
            + "  \"Reschedule\":null,\n"
            + "  \"Migrate\":null,\n"
            + "  \"Meta\":null,\n"
            + "  \"VaultToken\":\"vault-token\",\n"
            + "  \"Status\":null,\n"
            + "  \"StatusDescription\":null,\n"
            + "  \"Stable\":null,\n"
            + "  \"Version\":null,\n"
            + "  \"SubmitTime\":null,\n"
            + "  \"CreateIndex\":null,\n"
            + "  \"ModifyIndex\":null,\n"
            + "  \"JobModifyIndex\":null\n"
            + "}";

        ObjectMapper mapper = new ObjectMapper();
        final JsonNode actualJsonNode = mapper.readTree(job.toString());
        final JsonNode expectedJsonNode = mapper.readTree(expectedJob);

        assertThat(actualJsonNode, is(expectedJsonNode));
    }

    @Test(enabled = false)
    public void test() throws InterruptedException {
        List<String> list = new ArrayList<>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");
        list.add("six");

        AtomicInteger counter = new AtomicInteger();

        Executor executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(list.size());
        for (String jobName : list) {
            executor.execute(() -> {
                int id = counter.getAndIncrement();
                long timeout = System.currentTimeMillis() + 5 * 1_000;
                logger.info("{}: {}", jobName, id + 1);
                while (System.currentTimeMillis() < timeout) {
                    logger.info("{}, {}", id, System.currentTimeMillis());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                latch.countDown();
            });
        }

        latch.await(60 * 1_000, TimeUnit.MILLISECONDS);
        ((ExecutorService) executor).shutdown();

    }
}
