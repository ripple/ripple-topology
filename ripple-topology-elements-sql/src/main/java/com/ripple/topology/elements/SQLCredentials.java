package com.ripple.topology.elements;

import java.util.Objects;

/**
 * @author jfulton
 */
public class SQLCredentials {

    private String username;
    private String password;

    public SQLCredentials() {
        // Jackson Only
    }

    public SQLCredentials(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public SQLCredentials setUsername(final String username) {
        this.username = Objects.requireNonNull(username);
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SQLCredentials setPassword(final String password) {
        this.password = Objects.requireNonNull(password);
        return this;
    }
}
