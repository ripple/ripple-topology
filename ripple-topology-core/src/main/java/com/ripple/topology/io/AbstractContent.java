package com.ripple.topology.io;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author jfulton
 */
public abstract class AbstractContent implements Content {

    @Override
    public boolean exists() {
        // Try file existence: can we find the file in the file system?
        try {
            return getFile().exists();
        }
        catch (IOException ex) {
            // Fall back to stream existence: can we open the stream?
            try {
                InputStream is = getInputStream();
                is.close();
                return true;
            }
            catch (Throwable isEx) {
                return false;
            }
        }
    }

    @Override
    public URL getURL() throws IOException {
        throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
    }

    @Override
    public URI getURI() throws IOException {
        URL url = getURL();
        try {
            return ContentUtils.toURI(url);
        }
        catch (URISyntaxException ex) {
            throw new RuntimeException("Invalid URI [" + url + "]", ex);
        }
    }

    @Override
    public long contentLength() throws IOException {
        InputStream is = getInputStream();
        Preconditions.checkArgument(is != null, "Resource InputStream must not be null");
        try {
            long size = 0;
            byte[] buf = new byte[255];
            int read;
            while ((read = is.read(buf)) != -1) {
                size += read;
            }
            return size;
        }
        finally {
            try {
                is.close();
            }
            catch (IOException ex) {
            }
        }
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this ||
            (obj instanceof Content && ((Content) obj).getDescription().equals(getDescription())));
    }

    @Override
    public int hashCode() {
        return getDescription().hashCode();
    }

    public abstract File getFile() throws IOException;
}
