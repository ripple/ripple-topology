package com.ripple.topology.io;

import com.google.common.base.Preconditions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * @author jfulton
 */
public class ClassPathContent extends AbstractFileResolvingContent {

    private final String path;
    private ClassLoader classLoader;
    private Class<?> clazz;

    public ClassPathContent(String path) {
        this(path, (ClassLoader) null);
    }

    public ClassPathContent(String path, ClassLoader classLoader) {
        Preconditions.checkNotNull(path, "path");
        String pathToUse = StringUtils.cleanPath(path);
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        this.path = pathToUse;
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }

    public ClassPathContent(String path, Class<?> clazz) {
        Preconditions.checkNotNull(path, "path");
        this.path = StringUtils.cleanPath(path);
        this.clazz = clazz;
    }

    protected ClassPathContent(String path, ClassLoader classLoader, Class<?> clazz) {
        this.path = StringUtils.cleanPath(path);
        this.classLoader = classLoader;
        this.clazz = clazz;
    }

    public final String getPath() {
        return this.path;
    }

    @Override
    public boolean exists() {
        return (resolveURL() != null);
    }

    URL resolveURL() {
        if (this.clazz != null) {
            return this.clazz.getResource(this.path);
        }
        else if (this.classLoader != null) {
            return this.classLoader.getResource(this.path);
        }
        else {
            return ClassLoader.getSystemResource(this.path);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is;
        if (this.clazz != null) {
            is = this.clazz.getResourceAsStream(this.path);
        }
        else if (this.classLoader != null) {
            is = this.classLoader.getResourceAsStream(this.path);
        }
        else {
            is = ClassLoader.getSystemResourceAsStream(this.path);
        }
        if (is == null) {
            throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
        }
        return is;
    }

    @Override
    public URL getURL() throws IOException {
        URL url = resolveURL();
        if (url == null) {
            throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
        }
        return url;
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder("class path resource [");
        String pathToUse = path;
        if (this.clazz != null && !pathToUse.startsWith("/")) {
            builder.append(ClassUtils.classPackageAsResourcePath(this.clazz));
            builder.append('/');
        }
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        builder.append(pathToUse);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ClassPathContent) {
            ClassPathContent otherRes = (ClassPathContent) obj;
            return (this.path.equals(otherRes.path) &&
                Objects.equals(this.classLoader, otherRes.classLoader) &&
                Objects.equals(this.clazz, otherRes.clazz));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }
}
