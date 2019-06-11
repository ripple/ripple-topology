package com.ripple.topology;

import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadApiConfiguration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jfulton
 */
public class NomadClientFactory {

    private static final Map<String, NomadApiClient> clients = new HashMap<>();

    public static NomadApiClient getClient(String url) {
        return clients.computeIfAbsent(url, s -> {
            NomadApiConfiguration config =
                new NomadApiConfiguration.Builder()
                    .setAddress(s)
                    // Determine how to store auth token and CA path
                    //.setTlsCaFile(/* filePath */)
                    .setTlsSkipVerify(true)
                    .build();
            return new NomadApiClient(config);
        });
    }
}
