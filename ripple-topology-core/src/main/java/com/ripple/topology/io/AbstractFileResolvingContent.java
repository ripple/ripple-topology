package com.ripple.topology.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author jfulton
 */
public abstract class AbstractFileResolvingContent extends AbstractContent {

    @Override
    public File getFile() throws IOException {
        URL url = getURL();
        return ContentUtils.getFile(url, getDescription());
    }

    protected File getFile(URI uri) throws IOException {
        return ContentUtils.getFile(uri, getDescription());
    }

    @Override
    public boolean exists() {
        try {
            URL url = getURL();
            if (ContentUtils.isFileURL(url)) {
                // Proceed with file system resolution...
                return getFile().exists();
            }
            else {
                // Try a URL connection content-length header...
                URLConnection con = url.openConnection();
                customizeConnection(con);
                HttpURLConnection httpCon =
                    (con instanceof HttpURLConnection ? (HttpURLConnection) con : null);
                if (httpCon != null) {
                    int code = httpCon.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        return true;
                    }
                    else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return false;
                    }
                }
                if (con.getContentLength() >= 0) {
                    return true;
                }
                if (httpCon != null) {
                    // no HTTP OK status, and no content-length header: give up
                    httpCon.disconnect();
                    return false;
                }
                else {
                    // Fall back to stream existence: can we open the stream?
                    InputStream is = getInputStream();
                    is.close();
                    return true;
                }
            }
        }
        catch (IOException ex) {
            return false;
        }
    }

    private void customizeConnection(URLConnection con) throws IOException {
        ContentUtils.useCachesIfNecessary(con);
        if (con instanceof HttpURLConnection) {
            customizeConnection((HttpURLConnection) con);
        }
    }

    private void customizeConnection(HttpURLConnection con) throws IOException {
        con.setRequestMethod("HEAD");
    }
}
