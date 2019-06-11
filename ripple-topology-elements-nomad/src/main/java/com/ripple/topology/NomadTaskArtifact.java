package com.ripple.topology;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;

/**
 * @author jfulton
 */
@JsonPropertyOrder({"source", "destination"})
public class NomadTaskArtifact {

    private String mode = null;
    private String destination;
    private String source;

    public NomadTaskArtifact() {
    }

    public NomadTaskArtifact(final String source, final String destination) {
        this.destination = destination;
        this.source = source;
    }

    public NomadTaskArtifact(final String mode, final String source, final String destination) {
        this.mode = mode;
        this.destination = destination;
        this.source = source;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(final String mode) {
        this.mode = Objects.requireNonNull(mode);
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(final String destination) {
        this.destination = Objects.requireNonNull(destination);
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = Objects.requireNonNull(source);
    }
}
