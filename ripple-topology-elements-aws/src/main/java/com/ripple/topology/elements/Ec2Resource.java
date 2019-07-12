package com.ripple.topology.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ripple.topology.Lifecycle;
import com.ripple.topology.Topology;
import com.ripple.topology.VariableResolver;
import com.ripple.topology.VariableResolverAware;
import com.ripple.topology.utils.HealthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.Ec2ClientBuilder;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author jfulton
 */
@JsonInclude(Include.NON_DEFAULT)
public class Ec2Resource extends AbstractPropertiesAndEnvironmentAwareResource<Ec2Resource> implements Lifecycle,
    VariableResolverAware, HostResource {

    private static final Logger logger = LoggerFactory.getLogger(Ec2Resource.class);

    private boolean dryRun = false;
    private String ami;
    private InstanceType instanceType = InstanceType.T1_MICRO;
    private Region region = Region.US_WEST_1;
    private String keyPair;
    private String securityGroup;
    private int minCount = 1;
    private int maxCount = 1;
    private List<String> userData = new ArrayList<>();
    private int javaXmx = 512;
    private int javaXms = 256;
    private String host;
    private final List<String> instanceIds = new ArrayList<>();


    public Ec2Resource() {
        super("");
    }

    public Ec2Resource(final String key, final String ami, final InstanceType instanceType, final String keyPair,
        final String securityGroup) {
        super(key);
        this.ami = Objects.requireNonNull(ami);
        this.instanceType = Objects.requireNonNull(instanceType);
        this.keyPair = Objects.requireNonNull(keyPair);
        this.securityGroup = Objects.requireNonNull(securityGroup);
    }

    @Override
    public CompletableFuture<Void> start(final Topology topology) {
        return CompletableFuture.runAsync(() -> {
            if (!dryRun) {
                deploy();
            }
        });
    }

    @Override
    public CompletableFuture<Void> stop(final Topology topology) {
        return CompletableFuture.runAsync(() -> {
            if (!dryRun) {
                undeploy();
            }
        });
    }

    private void deploy() {
        Ec2ClientBuilder clientBuilder = Ec2Client.builder()
            .region(Region.of(getRegion()));
        customizeEc2Client(clientBuilder);
        Ec2Client client = clientBuilder.build();

        RunInstancesRequest.Builder runInstancesRequest = RunInstancesRequest.builder()
            .imageId(getAmi())
            .instanceType(getInstanceType())
            .minCount(getMinCount())
            .maxCount(getMaxCount())
            .keyName(getKeyPair())
            .userData(getCalculatedUserDataBase64())
            .securityGroups(getSecurityGroup());
        customizeRunInstancesRequest(runInstancesRequest);

        RunInstancesResponse runInstancesResponse = client.runInstances(runInstancesRequest.build());
        handleRunInstancesResponse(runInstancesResponse);
        final List<String> ids = runInstancesResponse.instances().stream().map(i -> i.instanceId())
            .collect(Collectors.toList());
        instanceIds.addAll(ids);

        Instance instance = runInstancesResponse.instances().get(0);
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().instanceIds(instance.instanceId())
            .build();

        HealthUtils.waitForHealth(Duration.ofMinutes(1), Duration.ofMillis(500), "'" + getKey() + "' Ec2 Instance", () -> {
            DescribeInstancesResponse describeInstancesResponse = client.describeInstances(request);
            String ipAddress = describeInstancesResponse.reservations().get(0).instances().get(0).publicIpAddress();
            if (ipAddress == null) {
                return false;
            } else {
                setHost(ipAddress);
                return true;
            }
        });
    }

    private void undeploy() {
        Ec2ClientBuilder clientBuilder = Ec2Client.builder()
            .region(Region.of(getRegion()));
        customizeEc2Client(clientBuilder);
        Ec2Client client = clientBuilder.build();

        final TerminateInstancesRequest terminateInstancesRequest = TerminateInstancesRequest.builder()
            .instanceIds(instanceIds).build();

        client.terminateInstances(terminateInstancesRequest);
    }

    public String getAmi() {
        return ami;
    }

    public Ec2Resource setAmi(final String ami) {
        this.ami = Objects.requireNonNull(ami);
        return this;
    }

    public InstanceType getInstanceType() {
        return instanceType;
    }

    public Ec2Resource setInstanceType(final InstanceType instanceType) {
        this.instanceType = Objects.requireNonNull(instanceType);
        return this;
    }

    public String getKeyPair() {
        return keyPair;
    }

    public Ec2Resource setKeyPair(final String keyPair) {
        this.keyPair = Objects.requireNonNull(keyPair);
        return this;
    }

    public String getSecurityGroup() {
        return securityGroup;
    }

    public Ec2Resource setSecurityGroup(final String securityGroup) {
        this.securityGroup = Objects.requireNonNull(securityGroup);
        return this;
    }

    public int getMinCount() {
        return minCount;
    }

    public Ec2Resource setMinCount(final int minCount) {
        this.minCount = minCount;
        return this;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public Ec2Resource setMaxCount(final int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    public String getRegion() {
        return region.id();
    }

    public Ec2Resource setRegion(final String region) {
        this.region = Region.of(Objects.requireNonNull(region));
        return this;
    }

    public final List<String> getUserData() {
        return userData;
    }

    public Ec2Resource addUserData(String userData) {
        getUserData().add(userData);
        return this;
    }

    @Override
    public String getHost() {
        return host;
    }

    public Ec2Resource setHost(final String host) {
        this.host = Objects.requireNonNull(host);
        return this;
    }

    @JsonIgnore
    public final String getCalculatedUserData() {
        StringBuilder builder = new StringBuilder();
        builder.append("#!/usr/bin/env bash\n\n");
        for (Entry<String, String> pair : getEnvironment().entrySet()) {
            builder.append("export ").append(pair.getKey()).append("=").append("\"").append(pair.getValue()).append("\"\n");
        }
        builder.append("export JAVA_MEM=\"-Xmx").append(javaXmx).append("m ").append("-Xms").append(javaXms).append("m\"\n");
        if (getProperties().size() > 0) {
            builder.append("export JAVA_OPTS=\"");
            AtomicBoolean firstProperty = new AtomicBoolean(true);
            for (String key : getProperties().stringPropertyNames()) {
                if (!firstProperty.getAndSet(false)) {
                    builder.append(" ");
                }
                builder.append("-D").append(key).append("=").append(getProperties().getProperty(key));
            }
            builder.append("\"\n");
        }
        customizeUserData(builder);
        for (String data : getUserData()) {
            builder.append(data).append("\n");
        }
        return builder.toString();
    }

    @JsonIgnore
    public final String getCalculatedUserDataBase64() {
        return Base64.getEncoder().encodeToString(getCalculatedUserData().getBytes());
    }

    public int getJavaXmx() {
        return javaXmx;
    }

    public Ec2Resource setJavaXmx(final int javaXmx) {
        this.javaXmx = javaXmx;
        return this;
    }

    public int getJavaXms() {
        return javaXms;
    }

    public Ec2Resource setJavaXms(final int javaXms) {
        this.javaXms = javaXms;
        return this;
    }

    protected void customizeRunInstancesRequest(final RunInstancesRequest.Builder runInstanceRequestBuilder) {
    }

    protected void customizeEc2Client(Ec2ClientBuilder clientBuilder) {
    }

    protected void customizeUserData(StringBuilder stringBuilder) {
    }

    protected void handleRunInstancesResponse(final RunInstancesResponse runInstancesResponse) {
    }

    @Override
    public void resolveVariables(final Topology topology, final VariableResolver resolver) {
        for (int i = 0; i < userData.size(); i++) {
            userData.set(i, resolver.resolve(userData.get(i)));
        }
    }

    @JsonIgnore
    public Ec2Resource setDryRun(final boolean disableDeploy) {
        this.dryRun = disableDeploy;
        return this;
    }
}
