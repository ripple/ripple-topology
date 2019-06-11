package com.ripple.topology;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.net.HostAndPort;
import com.hashicorp.nomad.apimodel.NetworkResource;
import com.hashicorp.nomad.apimodel.Port;
import com.hashicorp.nomad.apimodel.Resources;
import com.hashicorp.nomad.apimodel.Service;
import com.hashicorp.nomad.apimodel.ServiceCheck;
import com.hashicorp.nomad.apimodel.Task;
import com.hashicorp.nomad.apimodel.TaskArtifact;
import com.hashicorp.nomad.apimodel.TaskGroup;
import com.hashicorp.nomad.apimodel.Template;
import com.ripple.topology.elements.AbstractPropertiesAndEnvironmentAwareResource;
import com.ripple.topology.elements.HostAndPortResource;
import com.ripple.topology.elements.HttpUrlResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import okhttp3.HttpUrl;

/**
 * @author jfulton
 * @author mphinney
 */
public class NomadTask extends AbstractPropertiesAndEnvironmentAwareResource<NomadTask> implements VariableResolverAware,
    HostAndPortResource, HttpUrlResource {

    private final List<NomadTaskArtifact> mounts = new ArrayList<>();
    private final List<NomadTaskTemplate> templates = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private final List<String> volumes = new ArrayList<>();
    private String httpCheck = null;
    private String image;
    private int port;
    private HostAndPort hostAndPort;
    private HttpUrl httpUrl;
    private int maxCpu = 256;
    private int maxRam = 64;

    public NomadTask() {
        super("");
    }

    public NomadTask(String key, String image, final int port) {
        super(key);
        this.image = image;
        this.port = port;
    }

    @JsonIgnore
    public TaskGroup getTaskGroup() {
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.addTasks(getTask());
        taskGroup.setName(getKey());
        return taskGroup;
    }

    @JsonIgnore
    public Task getTask() {
        Task task = new Task();
        task.setName(getKey());
        task.setDriver("docker");
        task.addConfig("image", getImage());

        task.addConfig("port_map", Arrays.asList(Collections.singletonMap(getKey(), getPort())));

        task.setResources(getResources());

        if (getVolumes().size() > 0) {
            task.addConfig("volumes", getVolumes());
        }

        if (!getProperties().isEmpty()) {
            StringBuilder javaOpts = new StringBuilder();
            if (getEnvironment().containsKey("JAVA_OPTS")) {
                javaOpts.append(getEnvironment().get("JAVA_OPTS"));
            }
            getProperties().forEach((k, v) -> javaOpts.append(" -D").append(k).append("=").append(v));
            getEnvironment().put("JAVA_OPTS", javaOpts.toString());
        }

        if (!getEnvironment().containsKey("JAVA_MEM")) {
            getEnvironment().put("JAVA_MEM", "-Xmx" + getMaxRam() + "m");
        }

        getEnvironment().forEach(task::addEnv);

        for (NomadTaskArtifact nomadTaskArtifact : getArtifacts()) {
            TaskArtifact artifact = new TaskArtifact();
            artifact.setGetterSource(nomadTaskArtifact.getSource());
            artifact.setRelativeDest(nomadTaskArtifact.getDestination());
            if (nomadTaskArtifact.getMode() == null) {
                artifact.setGetterMode("any");
            } else {
                artifact.setGetterMode(nomadTaskArtifact.getMode());
            }
            task.addArtifacts(artifact);
        }

        for (NomadTaskTemplate taskTemplate : getTemplates()) {
            Template template = new Template();
            template.setSourcePath(taskTemplate.getSource());
            template.setDestPath(taskTemplate.getDestination());
            task.addTemplates(template);
        }

        task.addServices(getService());

        // Force docker to re-pull images on SNAPSHOT builds
        if (getImage().contains("SNAPSHOT")) {
            task.addConfig("force_pull", true);
        }

        return task;
    }

    @JsonIgnore
    public Service getService() {
        Service service = new Service();
        service.setName(getKey());
        service.setPortLabel(getKey());

        ServiceCheck tcpCheck = new ServiceCheck();
        tcpCheck.setName(getKey() + "-tcp-check");
        tcpCheck.setType("tcp");
        tcpCheck.setPortLabel(getKey());
        tcpCheck.setTlsSkipVerify(false);
        tcpCheck.setInterval(100000000000L);
        tcpCheck.setTimeout(2000000000L);
        service.addChecks(tcpCheck);

        if (isHttpEnabled()) {
            ServiceCheck httpCheck = new ServiceCheck();
            httpCheck.setName(getKey() + "-http-check");
            httpCheck.setType("http");
            httpCheck.setPath(getHttpCheck());
            httpCheck.setPortLabel(getKey());
            httpCheck.setTlsSkipVerify(false);
            httpCheck.setInterval(100000000000L);
            httpCheck.setTimeout(2000000000L);
            service.addChecks(httpCheck);
        }
        
        if (getTags().size() > 0) {
            for (String tag : getTags()) {
                service.addTags(tag);
            }
        }
        return service;
    }

    @JsonIgnore
    protected Resources getResources() {
        Resources resources = new Resources();
        resources.setCpu(getMaxCpu());
        resources.setMemoryMb(getMaxRam());

        NetworkResource networkResource = new NetworkResource();
        Port port = new Port();
        port.setLabel(getKey());
        networkResource.addDynamicPorts(port);
        resources.addNetworks(networkResource);
        return resources;
    }

    public List<NomadTaskArtifact> getArtifacts() {
        return mounts;
    }

    public NomadTask addArtifact(String source, String destination) {
        getArtifacts().add(new NomadTaskArtifact(source, destination));
        return this;
    }

    public NomadTask addArtifact(String mode, String source, String destination) {
        getArtifacts().add(new NomadTaskArtifact(mode, source, destination));
        return this;
    }

    public List<NomadTaskTemplate> getTemplates() {
        return templates;
    }

    public NomadTask addTemplate(String source, String destination) {
        getTemplates().add(new NomadTaskTemplate(source, destination));
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public NomadTask addTag(String tag) {
        getTags().add(tag);
        return this;
    }

    public List<String> getVolumes() {
        return volumes;
    }

    public NomadTask addVolume(String volume) {
        getVolumes().add(volume);
        return this;
    }

    public NomadTask addVolume(String source, String destination) {
        addVolume(source + ":" + destination);
        return this;
    }


    public int getPort() {
        return port;
    }

    public NomadTask setPort(int port) {
        this.port = port;
        return this;
    }

    public int getMaxCpu() {
        return maxCpu;
    }

    public void setMaxCpu(final int maxCpu) {
        this.maxCpu = maxCpu;
    }

    public int getMaxRam() {
        return maxRam;
    }

    public void setMaxRam(final int maxRam) {
        this.maxRam = maxRam;
    }

    public String getImage() {
        return image;
    }

    public void setImage(final String image) {
        this.image = image;
    }

    public String getHttpCheck() {
        return httpCheck;
    }

    public NomadTask setHttpCheck(final String httpCheck) {
        this.httpCheck = httpCheck;
        return this;
    }

    @JsonIgnore
    public boolean isHttpEnabled() {
        return httpCheck != null;
    }

    @Override
    public void resolveVariables(final Topology topology, final VariableResolver resolver) {
        this.image = resolver.resolve(image);
        for (NomadTaskArtifact artifact : getArtifacts()) {
            artifact.setDestination(resolver.resolve(artifact.getDestination()));
            artifact.setSource(resolver.resolve(artifact.getSource()));
        }

        for (NomadTaskTemplate template : getTemplates()) {
            template.setDestination(resolver.resolve(template.getDestination()));
            template.setSource(resolver.resolve(template.getSource()));
        }

        for (int i = 0; i < getTags().size(); i++) {
            tags.set(i, resolver.resolve(tags.get(i)));
        }

        for (int i = 0; i < getVolumes().size(); i++) {
            volumes.set(i, resolver.resolve(volumes.get(i)));
        }
    }

    @Override
    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    @Override
    public HttpUrl getHttpUrl() {
        return httpUrl;
    }

    public void setHostAndPort(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public void setHttpUrl(HttpUrl httpUrl) {
        this.httpUrl = httpUrl;
    }
}
