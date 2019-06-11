package com.ripple.topology;

/**
 * @author jfulton
 */
public class NomadTaskTemplate {

    private String destination;
    private String source;

    public NomadTaskTemplate() {
        // Jackson
    }

    public NomadTaskTemplate(final String source, final String destination) {
        this.source = source;
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(final String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }
}
