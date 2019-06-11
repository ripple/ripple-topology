package com.ripple.topology.io;

/**
 * @author jfulton
 */
public interface ContentLoader {

    String CLASSPATH_URL_PREFIX = ContentUtils.CLASSPATH_URL_PREFIX;

    Content getContent(String location);
}
