package com.ripple.topology.ui;


import com.google.common.net.HostAndPort;
import com.ripple.topology.Topology;
import com.ripple.topology.elements.StaticHostAndPort;
import com.ripple.topology.elements.StaticHttpUrl;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfulton
 */
public class TopologyServerMain {

    private static final Logger logger = LoggerFactory.getLogger(TopologyServerMain.class);

    public static void main(String[] args) {
        Topology topology = new Topology()
            .addElement(new StaticHttpUrl("g.eu.fr.nice.", HttpUrl.parse("http://10.20.25.112:54440/")))
            .addElement(new StaticHostAndPort("g.eu.fr.nice.", HostAndPort.fromString("10.20.25.112:54440")))
            ;

        new TopologyUI("South America", topology, 9090).start();
        new TopologyUI("North America", topology, 8080).start();
    }
}
