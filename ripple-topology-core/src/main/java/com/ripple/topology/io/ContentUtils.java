package com.ripple.topology.io;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public abstract class ContentUtils {

    public static final String CLASSPATH_URL_PREFIX = "classpath:";

    public static final String URL_PROTOCOL_FILE = "file";

    public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
        Preconditions.checkNotNull(resourceUrl, "resourceUrl");
        if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
            throw new FileNotFoundException(
                description + " cannot be resolved to absolute file path " +
                    "because it does not reside in the file system: " + resourceUrl);
        }
        try {
            return new File(toURI(resourceUrl).getSchemeSpecificPart());
        } catch (URISyntaxException ex) {
            // Fallback for URLs that are not valid URIs (should hardly ever happen).
            return new File(resourceUrl.getFile());
        }
    }

    public static File getFile(URI resourceUri, String description) throws FileNotFoundException {
        Preconditions.checkNotNull(resourceUri, "resourceUri");
        if (!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
            throw new FileNotFoundException(
                description + " cannot be resolved to absolute file path " +
                    "because it does not reside in the file system: " + resourceUri);
        }
        return new File(resourceUri.getSchemeSpecificPart());
    }

    public static boolean isFileURL(URL url) {
        String protocol = url.getProtocol();
        return (URL_PROTOCOL_FILE.equals(protocol));
    }

    public static URI toURI(URL url) throws URISyntaxException {
        return toURI(url.toString());
    }

    public static URI toURI(String location) throws URISyntaxException {
        return new URI(StringUtils.replace(location, " ", "%20"));
    }

    public static void useCachesIfNecessary(URLConnection con) {
        con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
    }

}
