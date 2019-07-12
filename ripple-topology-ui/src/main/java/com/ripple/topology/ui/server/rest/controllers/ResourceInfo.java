package com.ripple.topology.ui.server.rest.controllers;

import java.util.Objects;

public class ResourceInfo {

    private String key;
    private String endpoint;
    private String type;

    public ResourceInfo(String key, String endpoint, String type) {
        this.key = Objects.requireNonNull(key, "'key' cannot be null");
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.type = Objects.requireNonNull(type, "'type' cannot be null");
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = Objects.requireNonNull(type);
    }
}
