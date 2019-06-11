package com.ripple.topology.serialization.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.io.IOException;
import okhttp3.HttpUrl;

/**
 * An extension of {@link SimpleModule} for registering with Jackson {@link ObjectMapper} to serialize instances of
 * {@link HttpUrl}.
 *
 * @see "http://wiki.fasterxml.com/JacksonMixInAnnotations"
 */
public class HttpUrlModule extends SimpleModule {

    /**
     * No-args Constructor.
     */
    public HttpUrlModule() {
        super(
            "HttpUrlModule",
            new Version(1, 0, 0, null, "com.squareup.okhttp3", "okhttp")
        );

        this.addSerializer(new ToStringSerializer(HttpUrl.class));
        this.addDeserializer(HttpUrl.class, new HttpUrlDeserializer());
    }

    /**
     * An extension of {@link FromStringDeserializer} that deserializes a JSON string into an instance of {@link
     * HttpUrl}.
     */
    public static class HttpUrlDeserializer extends FromStringDeserializer<HttpUrl> {

        /**
         * No-args Constructor.
         */
        public HttpUrlDeserializer() {
            super(HttpUrl.class);
        }

        @Override
        public HttpUrl deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return HttpUrl.parse(p.getValueAsString());
        }

        @Override
        protected HttpUrl _deserialize(final String s, final DeserializationContext deserializationContext)
            throws IOException {
            return HttpUrl.parse(s);
        }
    }
}
