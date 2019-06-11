package com.ripple.topology.serialization.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.net.HostAndPort;
import java.io.IOException;

/**
 * @author jfulton
 */
public class HostAndPortModule extends SimpleModule {

    public HostAndPortModule() {
        super(
            "HostAndPortModule",
            new Version(27, 0, 1, null, "com.google.guave", "guava")
        );

        this.addSerializer(new ToStringSerializer(HostAndPort.class));
        this.addDeserializer(HostAndPort.class, new HostAndPortDeserializer());
    }

    public static class HostAndPortDeserializer extends FromStringDeserializer<HostAndPort> {

        /**
         * No-args Constructor.
         */
        public HostAndPortDeserializer() {
            super(HostAndPort.class);
        }

        @Override
        public HostAndPort deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return HostAndPort.fromString(p.getValueAsString());
        }

        @Override
        protected HostAndPort _deserialize(final String s, final DeserializationContext deserializationContext)
            throws IOException {
            return HostAndPort.fromString(s);
        }
    }
}
