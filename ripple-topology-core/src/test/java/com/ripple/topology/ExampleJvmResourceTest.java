package com.ripple.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author jfulton
 */
public class ExampleJvmResourceTest {

    private static final Logger logger = LoggerFactory.getLogger(ExampleJvmResourceTest.class);

    @Test
    public void testApi() {
        ExampleJvmResource resource = new ExampleJvmResource("sf").setXmx(512).setXms(256).addProperty("key", "value");
        logger.info("{}", resource);
    }
}
