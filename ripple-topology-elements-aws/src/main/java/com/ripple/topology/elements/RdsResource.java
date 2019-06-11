package com.ripple.topology.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.net.HostAndPort;
import com.ripple.topology.Lifecycle;
import com.ripple.topology.Topology;
import com.ripple.topology.VariableResolver;
import com.ripple.topology.VariableResolverAware;
import com.ripple.topology.utils.HealthUtils;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbInstanceRequest;
import software.amazon.awssdk.services.rds.model.DeleteDbInstanceRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.Endpoint;

/**
 * @author matt
 */
public class RdsResource extends AbstractEnvironmentAwareResource implements Lifecycle,
    VariableResolverAware, HostAndPortResource {

    private Region region = Region.US_WEST_1;
    private String instanceClass;
    private int allocatedStorage;
    private String instanceId;
    private String engine;
    private String userName;
    private String password;
    private int port;
    private HostAndPort hostAndPort;
    private boolean dryRun = false;

    public RdsResource() {
        super("");
    }

    public RdsResource(String key, String instanceClass, int allocatedStorage, String instanceId,
        String engine, String userName, String password) {
        super(key);
        this.instanceClass = instanceClass;
        this.allocatedStorage = allocatedStorage;
        this.instanceId = instanceId;
        this.engine = engine;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public CompletableFuture<Void> start(Topology topology) {
        return CompletableFuture.runAsync(() -> {
            if (!dryRun) {
                deploy();
            }
        });
    }

    @Override
    public CompletableFuture<Void> stop(Topology topology) {
        return CompletableFuture.runAsync(() -> {
            if (!dryRun) {
                undeploy();
            }
        });
    }

    @Override
    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public String getInstanceClass() {
        return instanceClass;
    }

    public RdsResource setInstanceClass(String instanceClass) {
        this.instanceClass = Objects.requireNonNull(instanceClass);
        return this;
    }

    public int getAllocatedStorage() {
        return allocatedStorage;
    }

    public RdsResource setAllocatedStorage(int allocatedStorage) {
        this.allocatedStorage = allocatedStorage;
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public RdsResource setInstanceId(String instanceId) {
        this.instanceId = Objects.requireNonNull(instanceId);
        return this;
    }

    public String getEngine() {
        return engine;
    }

    public RdsResource setEngine(String engine) {
        this.engine = Objects.requireNonNull(engine);
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public RdsResource setUserName(String userName) {
        this.userName = Objects.requireNonNull(userName);
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RdsResource setPassword(String password) {
        this.password = Objects.requireNonNull(password);
        return this;
    }

    public int getPort() {
        return port;
    }

    public RdsResource setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    @JsonIgnore
    public RdsResource setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
        return this;
    }

    public String getRegion() {
        return region.id();
    }

    public RdsResource setRegion(Region region) {
        this.region = Objects.requireNonNull(region);
        return this;
    }

    private void deploy() {
        final RdsClient client = RdsClient.builder().region(Region.of(getRegion())).build();

        final CreateDbInstanceRequest request = CreateDbInstanceRequest.builder()
            .dbInstanceIdentifier(instanceId)
            .allocatedStorage(allocatedStorage)
            .dbInstanceClass(instanceClass)
            .engine(engine)
            .port(port)
            .masterUsername(userName)
            .masterUserPassword(password)
            .build();
        client.createDBInstance(request);

        HealthUtils.waitForHealth(Duration.ofMinutes(10), Duration.ofMillis(2000), "'" + getKey() + "' RDS Instance", () -> {
            final DescribeDbInstancesRequest describeDbInstancesRequest = DescribeDbInstancesRequest.builder()
                .dbInstanceIdentifier(getInstanceId()).build();
            final Endpoint endpoint = client
                .describeDBInstances(describeDbInstancesRequest)
                .dbInstances().get(0).endpoint();
            if (endpoint == null) {
                return false;
            } else {
                setHostAndPort(HostAndPort.fromParts(endpoint.address(), endpoint.port()));
                return true;
            }
        });
    }

    private void undeploy() {
        final RdsClient client = RdsClient.builder().region(Region.of(getRegion())).build();
        final DeleteDbInstanceRequest request = DeleteDbInstanceRequest.builder().dbInstanceIdentifier(instanceId)
            .skipFinalSnapshot(true).build();
        client.deleteDBInstance(request);
    }

    public RdsResource setHostAndPort(HostAndPort hostAndPort) {
        this.hostAndPort = Objects.requireNonNull(hostAndPort);
        return this;
    }

    @Override
    public void resolveVariables(Topology topology, VariableResolver resolver) {

    }
}
