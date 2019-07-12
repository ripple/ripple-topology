package com.ripple.topology.ui.server.rest.controllers;

import com.ripple.topology.Resource;
import com.ripple.topology.Topology;
import com.ripple.topology.elements.HostAndPortResource;
import com.ripple.topology.elements.HttpUrlResource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class RootController {

    private final Topology topology;

    @Autowired
    public RootController(final Topology topology) {
        this.topology = topology;
    }

    @Autowired
    private Environment environment;

    @RequestMapping
    public ModelAndView nodes() {
        List<ResourceInfo> results = new ArrayList<>();
        topology.getElements(Resource.class)
            .forEach(node -> {
                if (node instanceof HttpUrlResource) {
                    HttpUrlResource resource = (HttpUrlResource)node;
                    results.add(new ResourceInfo(resource.getKey(), resource.getHttpUrl().toString(), "HTTP"));
                } else if (node instanceof HostAndPortResource) {
                    HostAndPortResource resource = (HostAndPortResource) node;
                    results.add(new ResourceInfo(resource.getKey(), resource.getHostAndPort().toString(),"TCP"));
                }
            });
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("resources", results);

        String title = environment.getProperty("topology.server.name");

        if (title != null && title.length() > 0) {
            mav.addObject("title",title + " Topology");
        } else {
            mav.addObject("title", "Topology");
        }

        return mav;
    }
}
