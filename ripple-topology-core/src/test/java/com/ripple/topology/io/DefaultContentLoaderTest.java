package com.ripple.topology.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class DefaultContentLoaderTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultContentLoaderTest.class);
    private ContentLoader loader = new DefaultContentLoader();

    @Test(dataProvider = "classPathExamples")
    public void testLoadFromClasspathWithRootPrefex(String path) {
        Content content = loader.getContent(path);
        assertThat(content.exists(), is(true));
        assertThat(content.getDescription(), is("class path resource [example/ExampleContent.txt]"));
    }

    @DataProvider
    public Object[][] classPathExamples() {
        return new Object[][] {
            {"/example/ExampleContent.txt"},
            {"example/ExampleContent.txt"},
            {"classpath:/example/ExampleContent.txt"},
            {"classpath:example/ExampleContent.txt"},
        };
    }

    @Test
    public void testAbsoluteFilePath() throws IOException {
        File file = File.createTempFile("example", ".txt");
        Writer writer = new BufferedWriter(new FileWriter(file));
        writer.write("Howdy Ho!");
        writer.close();
        URI fileUri = file.toURI();
        URL fileUrl = fileUri.toURL();
        Content content = loader.getContent(fileUrl.toString());
        assertThat(content.exists(), is(true));
        assertThat(content.getDescription(), is(String.format("URL [%s]", fileUrl)));
        assertThat(content.getURL(), is(fileUrl));
        assertThat(content.getURI(), is(fileUri));
        assertThat(content.asUTF8String(), is("Howdy Ho!"));
        logger.info("File URL: {}", content.getURL());
        logger.info("File URI: {}", content.getURI());

        file.delete();
    }
}
