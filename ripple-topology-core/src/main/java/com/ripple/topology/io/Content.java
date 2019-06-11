package com.ripple.topology.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;

public interface Content {

    boolean exists();

    URL getURL() throws IOException;

    URI getURI() throws IOException;

    File getFile() throws IOException;

    long contentLength() throws IOException;

    String getDescription();

    InputStream getInputStream() throws IOException;

    default String asUTF8String() {
        try (InputStream stream = getInputStream()) {
            return IOUtils.toString(stream, Charset.forName("UTF8"));
        } catch (IOException e) {
            throw new RuntimeException("Error reading String from " + getDescription(), e);
        }
    }
}
