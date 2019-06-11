package com.ripple.topology.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class UrlContentTest {

    @Test
    public void testHttpUrlExisting() throws MalformedURLException {
        UrlContent content = new UrlContent(new URL("http://www.google.com"));
        assertThat(content.exists(), is(true));
    }

    @Test
    public void testHttpUrlNotExisting() throws MalformedURLException {
        UrlContent content = new UrlContent(new URL("http://www.misssing.missing/"));
        assertThat(content.exists(), is(false));
    }

    @Test(enabled = false, groups = {"manual"})
    public void testFtpExisting() throws MalformedURLException {
        UrlContent content = new UrlContent(new URL("ftp://speedtest.tele2.net/5MB.zip"));
        assertThat(content.exists(), is(true));
    }

    @Test
    public void testFileUrlExisting() throws IOException {
        File file = new File("./src/test/resources/example.properties");
        URL url = file.toURI().toURL();
        UrlContent content = new UrlContent(url);
        assertThat(content.exists(), is(true));
        assertThat(content.contentLength(), is(greaterThan(0L)));
        String contents = content.asUTF8String();
        assertThat(contents, is("item1=one\nitem2=two\n"));
    }
}
