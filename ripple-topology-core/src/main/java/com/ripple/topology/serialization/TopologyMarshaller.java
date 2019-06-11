package com.ripple.topology.serialization;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.USE_NATIVE_TYPE_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ripple.topology.Element;
import com.ripple.topology.Topology;
import com.ripple.topology.serialization.modules.HostAndPortModule;
import com.ripple.topology.serialization.modules.HttpUrlModule;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ServiceLoader;

/**
 * @author jfulton
 */
public class TopologyMarshaller {

    private final ObjectMapper mapper;

    public TopologyMarshaller(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public static TopologyMarshaller forYaml() {
        ServiceLoader<Element> loader = ServiceLoader.load(Element.class);
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.disable(USE_NATIVE_TYPE_ID);
        yamlFactory.enable(MINIMIZE_QUOTES);
        ObjectMapper mapper = new ObjectMapper(yamlFactory)
            .registerModule(new HttpUrlModule())
            .registerModule(new HostAndPortModule())
            .setSerializationInclusion(NON_DEFAULT)
            ;
        for (Element element : loader) {
            mapper.getSubtypeResolver().registerSubtypes(new NamedType(element.getClass()));
        }
        return new TopologyMarshaller(mapper);
    }


    public Topology read(String topologyConfig) {
        try {
            return mapper.readValue(topologyConfig, Topology.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Topology read(InputStream topologyConfig) {
        try {
            return mapper.readValue(topologyConfig, Topology.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Topology readFromClasspath(String classpathResource) {
        return read(getClass().getResourceAsStream(classpathResource));
    }

    public Topology read(byte[] topologyConfig) {
        try {
            return mapper.readValue(topologyConfig, Topology.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String writeAsString(Topology topologyConfig) {
        try {
            return mapper.writeValueAsString(topologyConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(Topology topologyConfig, OutputStream outputStream) {
        try {
            mapper.writeValue(outputStream, topologyConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] writeAsBytes(Topology topologyConfig) {
        try {
            return mapper.writeValueAsBytes(topologyConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }
}
