package com.ripple.topology;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashicorp.nomad.apimodel.TaskGroup;
import org.testng.annotations.Test;

/**
 * @author matt
 */
public class NomadTaskTest {
                                                          
    @Test
    public void testCreateNomadApplicationResourceTest() throws Exception {
        final NomadTask nomadTask = new NomadTask("sf",
            "artifactory.ops.ripple.com:6555/ripplenet/applications/xcurrent:0.25-SNAPSHOT", 9000) {
        };

        nomadTask.addArtifact("https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-logback.xml", "etc/logback.xml")
            .addArtifact("https://artifactory.ops.ripple"
                + ".com/artifactory/ripplenet-topology/sf-rn-keystore.pkcs12", "etc/rn-keystore.pkcs12")
            .addArtifact("https://artifactory.ops.ripple"
                + ".com/artifactory/ripplenet-topology/sf-usage-keystore.p12", "etc/usage-keystore.p12")
            .addArtifact("https://artifactory.ops.ripple"
                + ".com/artifactory/ripplenet-topology/sf-xcurrent.properties.tpl", "local/xcurrent.properties.tpl")
            .addTemplate("local/xcurrent.properties.tpl", "etc/xcurrent.properties")
            .addTag("standalone.enable=true")
            .addTag("standalone.frontend.entryPoints=http,https")
            .addTag("standalone.frontend.rule=Host:${key}.usw2.testdev.ripple.com")
            .addVolume("etc", "/application/etc")
            .setHttpCheck("/")
        ;

        final TaskGroup taskGroup = nomadTask.getTaskGroup();

        final String expected = "{\n"
            + "  \"Name\":\"sf\",\n"
            + "  \"Count\":null,\n"
            + "  \"Constraints\":null,\n"
            + "  \"Tasks\":[\n"
            + "    {\n"
            + "      \"Name\":\"sf\",\n"
            + "      \"Driver\":\"docker\",\n"
            + "      \"User\":null,\n"
            + "      \"Config\":{\n"
            + "        \"image\":\"artifactory.ops.ripple.com:6555/ripplenet/applications/xcurrent:0.25-SNAPSHOT\",\n"
            + "        \"port_map\":[\n"
            + "          {\n"
            + "            \"sf\":9000\n"
            + "          }\n"
            + "        ],\n"
            + "        \"volumes\":[\n"
            + "          \"etc:/application/etc\"\n"
            + "            ],\n"
            + "            \"force_pull\": true\n"
            + "      },\n"
            + "      \"Constraints\":null,\n"
            + "      \"Env\":{\"JAVA_MEM\":\"-Xmx64m\"},\n"
            + "      \"Services\":[\n"
            + "        {\n"
            + "          \"Id\":null,\n"
            + "          \"Name\":\"sf\",\n"
            + "          \"Tags\":[\n"
            + "            \"standalone.enable=true\",\n"
            + "            \"standalone.frontend.entryPoints=http,https\",\n"
            + "            \"standalone.frontend.rule=Host:${key}.usw2.testdev.ripple.com\"\n"
            + "          ],\n"
            + "          \"CanaryTags\":null,\n"
            + "          \"PortLabel\":\"sf\",\n"
            + "          \"AddressMode\":null,\n"
            + "          \"Checks\":[\n"
            + "            {\n"
            + "              \"Id\":null,\n"
            + "              \"Name\":\"sf-tcp-check\",\n"
            + "              \"Type\":\"tcp\",\n"
            + "              \"Command\":null,\n"
            + "              \"Args\":null,\n"
            + "              \"Path\":null,\n"
            + "              \"Protocol\":null,\n"
            + "              \"PortLabel\":\"sf\",\n"
            + "              \"AddressMode\":null,\n"
            + "              \"Interval\":100000000000,\n"
            + "              \"Timeout\":2000000000,\n"
            + "              \"InitialStatus\":null,\n"
            + "              \"TLSSkipVerify\":false,\n"
            + "              \"Header\":null,\n"
            + "              \"Method\":null,\n"
            + "              \"CheckRestart\":null,\n"
            + "              \"GRPCService\":null,\n"
            + "              \"GRPCUseTLS\":false\n"
            + "            },\n"
            + "            {\n"
            + "              \"Id\":null,\n"
            + "              \"Name\":\"sf-http-check\",\n"
            + "              \"Type\":\"http\",\n"
            + "              \"Command\":null,\n"
            + "              \"Args\":null,\n"
            + "              \"Path\":\"/\",\n"
            + "              \"Protocol\":null,\n"
            + "              \"PortLabel\":\"sf\",\n"
            + "              \"AddressMode\":null,\n"
            + "              \"Interval\":100000000000,\n"
            + "              \"Timeout\":2000000000,\n"
            + "              \"InitialStatus\":null,\n"
            + "              \"TLSSkipVerify\":false,\n"
            + "              \"Header\":null,\n"
            + "              \"Method\":null,\n"
            + "              \"CheckRestart\":null,\n"
            + "              \"GRPCService\":null,\n"
            + "              \"GRPCUseTLS\":false\n"
            + "            }\n"
            + "          ],\n"
            + "          \"CheckRestart\":null\n"
            + "        }\n"
            + "      ],\n"
            + "      \"Resources\":{\n"
            + "        \"CPU\":256,\n"
            + "        \"MemoryMB\": 64,\n"
            + "        \"DiskMB\":null,\n"
            + "        \"IOPS\":null,\n"
            + "        \"Networks\":[\n"
            + "          {\n"
            + "            \"Device\":null,\n"
            + "            \"CIDR\":null,\n"
            + "            \"IP\":null,\n"
            + "            \"ReservedPorts\":null,\n"
            + "            \"DynamicPorts\":[\n"
            + "              {\n"
            + "                \"Label\":\"sf\",\n"
            + "                \"Value\":0\n"
            + "              }\n"
            + "            ],\n"
            + "            \"MBits\":null\n"
            + "          }\n"
            + "        ]\n"
            + "      },\n"
            + "      \"Meta\":null,\n"
            + "      \"KillTimeout\":null,\n"
            + "      \"LogConfig\":null,\n"
            + "      \"Artifacts\":[\n"
            + "        {\n"
            + "          \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-logback.xml\",\n"
            + "          \"GetterOptions\":null,\n"
            + "          \"GetterMode\":\"any\",\n"
            + "          \"RelativeDest\":\"etc/logback.xml\"\n"
            + "        },\n"
            + "        {\n"
            + "          \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-rn-keystore.pkcs12\",\n"
            + "          \"GetterOptions\":null,\n"
            + "          \"GetterMode\":\"any\",\n"
            + "          \"RelativeDest\":\"etc/rn-keystore.pkcs12\"\n"
            + "        },\n"
            + "        {\n"
            + "          \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-usage-keystore.p12\",\n"
            + "          \"GetterOptions\":null,\n"
            + "          \"GetterMode\":\"any\",\n"
            + "          \"RelativeDest\":\"etc/usage-keystore.p12\"\n"
            + "        },\n"
            + "        {\n"
            + "          \"GetterSource\":\"https://artifactory.ops.ripple"
            + ".com/artifactory/ripplenet-topology/sf-xcurrent.properties.tpl\",\n"
            + "          \"GetterOptions\":null,\n"
            + "          \"GetterMode\":\"any\",\n"
            + "          \"RelativeDest\":\"local/xcurrent.properties.tpl\"\n"
            + "        }\n"
            + "      ],\n"
            + "      \"Vault\":null,\n"
            + "      \"Templates\":[\n"
            + "        {\n"
            + "          \"SourcePath\":\"local/xcurrent.properties.tpl\",\n"
            + "          \"DestPath\":\"etc/xcurrent.properties\",\n"
            + "          \"EmbeddedTmpl\":null,\n"
            + "          \"ChangeMode\":null,\n"
            + "          \"ChangeSignal\":null,\n"
            + "          \"Splay\":null,\n"
            + "          \"Perms\":null,\n"
            + "          \"LeftDelim\":null,\n"
            + "          \"RightDelim\":null,\n"
            + "          \"Envvars\":null,\n"
            + "          \"VaultGrace\":null\n"
            + "        }\n"
            + "      ],\n"
            + "      \"DispatchPayload\":null,\n"
            + "      \"Leader\":false,\n"
            + "      \"ShutdownDelay\":0,\n"
            + "      \"KillSignal\":null\n"
            + "    }\n"
            + "  ],\n"
            + "  \"RestartPolicy\":null,\n"
            + "  \"ReschedulePolicy\":null,\n"
            + "  \"EphemeralDisk\":null,\n"
            + "  \"Update\":null,\n"
            + "  \"Migrate\":null,\n"
            + "  \"Meta\":null\n"
            + "}";

        ObjectMapper mapper = new ObjectMapper();
        final JsonNode actualJsonNode = mapper.readTree(taskGroup.toString());
        final JsonNode expectedJsonNode = mapper.readTree(expected);

        assertThat(actualJsonNode, is(expectedJsonNode));
    }
}
