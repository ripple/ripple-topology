package com.ripple.topology.elements;

import com.google.common.base.Preconditions;

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
        this.source = Preconditions.checkNotNull(source, "source");
        Preconditions.checkArgument(source.length() > 0);
        this.destination = Preconditions.checkNotNull(destination, "destination");
        Preconditions.checkArgument(destination.length() > 0);
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
