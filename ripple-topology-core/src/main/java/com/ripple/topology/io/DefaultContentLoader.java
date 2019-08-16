package com.ripple.topology.io;

import com.google.common.base.Preconditions;

import java.net.MalformedURLException;
import java.net.URL;

public class DefaultContentLoader implements ContentLoader {

    private ClassLoader classLoader = DefaultContentLoader.class.getClassLoader();

    public ClassLoader getClassLoader() {
        return (this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader());
    }

    @Override
    public Content getContent(String location) {
        Preconditions.checkNotNull(location, "location");

        if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            return new ClassPathContent(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
        } else {
            try {
                // Try to parse the location as a URL...
                URL url = new URL(location);
                return new UrlContent(url);
            } catch (MalformedURLException ex) {
                // No URL -> resolve as resource path.
                return getResourceByPath(location);
            }
        }
    }

    private Content getResourceByPath(String path) {
        return new ClassPathContextResource(path, getClassLoader());
    }


    protected static class ClassPathContextResource extends ClassPathContent {

        ClassPathContextResource(String path, ClassLoader classLoader) {
            super(path, classLoader);
        }
    }
}
