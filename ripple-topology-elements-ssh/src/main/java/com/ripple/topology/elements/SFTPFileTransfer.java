package com.ripple.topology.elements;

import com.ripple.runtime.Assert;
import java.util.Objects;

/**
 * @author jfulton
 */
public class SFTPFileTransfer {

    private String source;
    private String destination;

    public SFTPFileTransfer() {
        // Jackson Only
    }

    public SFTPFileTransfer(final String source, final String destination) {
        this.source = Assert.argumentNotBlank(source, "source");
        this.destination = Assert.argumentNotBlank(destination, "destination");
    }

    public String getSource() {
        return source;
    }

    public SFTPFileTransfer setSource(final String source) {
        this.source = Objects.requireNonNull(source);
        return this;
    }

    public String getDestination() {
        return destination;
    }

    public SFTPFileTransfer setDestination(final String destination) {
        this.destination = Objects.requireNonNull(destination);
        return this;
    }
}
