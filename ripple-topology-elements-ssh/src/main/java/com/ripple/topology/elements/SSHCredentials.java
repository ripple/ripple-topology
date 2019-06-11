package com.ripple.topology.elements;

import com.ripple.topology.io.Content;
import com.ripple.topology.io.ContentLoader;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.cactoos.io.TeeInput;

/**
 * @author jfulton
 */
public class SSHCredentials {

    private String username;
    private String privateKeyContents;
    private String privateKeyPath;

    public SSHCredentials() {
        // Jackson Only
    }

    public static SSHCredentials withUsernameAndKeyContents(String username, String privateKeyContents) {
        SSHCredentials credentials = new SSHCredentials();
        credentials.setUsername(username);
        credentials.setPrivateKeyContents(privateKeyContents);
        return credentials;
    }

    public static SSHCredentials withUsernameAndKeyPath(String username, String privateKeyPath) {
        SSHCredentials credentials = new SSHCredentials();
        credentials.setUsername(username);
        credentials.setPrivateKeyPath(privateKeyPath);
        return credentials;
    }

    public String getUsername() {
        return username;
    }

    public SSHCredentials setUsername(final String username) {
        this.username = Objects.requireNonNull(username);
        return this;
    }

    public String getPrivateKeyContents() {
        return privateKeyContents;
    }

    public SSHCredentials setPrivateKeyContents(final String privateKeyContents) {
        this.privateKeyContents = Objects.requireNonNull(privateKeyContents);
        return this;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public SSHCredentials setPrivateKeyPath(final String privateKeyPath) {
        this.privateKeyPath = Objects.requireNonNull(privateKeyPath);
        return this;
    }

    public void validate() {
        if ((getPrivateKeyPath() == null && getPrivateKeyContents() == null) || (getPrivateKeyContents() != null && getPrivateKeyPath() != null))
        {
            throw new RuntimeException("You must supply either the private key contents or the private key path, but not both");
        }
    }

    public File calculateKeyFile(ContentLoader contentLoader) {
        validate();

        if (getPrivateKeyPath() != null) {
            Content content = contentLoader.getContent(getPrivateKeyPath());
            try {
                return content.getFile();
            } catch (IOException ignored) {
            }

            return storeInTempFile(content.asUTF8String());
        } else {
            return storeInTempFile(getPrivateKeyContents());
        }
    }

    private File storeInTempFile(String key) {
        try {
            File file = File.createTempFile("topology-ssh", ".key");

            TeeInput teeInput = new TeeInput(key.replaceAll("\r", "")
                .replaceAll("\n\\s+|\n{2,}", "\n")
                .trim(), file);

            // Read Stream fully
            IOUtils.toByteArray(teeInput.stream());

            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
