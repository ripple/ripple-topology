package com.ripple.topology;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.net.HostAndPort;
import com.hashicorp.nomad.apimodel.AllocationListStub;
import com.hashicorp.nomad.apimodel.Evaluation;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.TaskState;
import com.hashicorp.nomad.javasdk.EvaluationResponse;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadException;
import com.ripple.topology.elements.AbstractElementGroup;
import com.ripple.topology.utils.HealthUtils;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jfulton
 * @author matt
 */
@JsonPropertyOrder({"variables", "clusterManager", "region", "datacenter", "groupId", "vaultToken"})
public class NomadJob extends AbstractElementGroup<NomadJob> implements Lifecycle, ScopedVariableSource<NomadJob>,
    VariableResolverAware {

    public static final Duration JOB_HEALTH_CHECK_TIMEOUT = Duration.ofMinutes(5);
    public static final Duration JOB_HEALTH_CHECK_PAUSE = Duration.ofSeconds(1);
    private static final String RUNNING = "running";

    private static final Logger logger = LoggerFactory.getLogger(NomadJob.class);

    private final Map<String, Object> variables = new LinkedHashMap<>();
    private HttpUrl clusterManager;
    private String groupId;
    private String vaultToken;
    private String region = "global";
    private String datacenter = "dc1";
    private int taskHealthCheckPause = 500;
    private int taskHealthTimeout = 90 * 1000;

    public HttpUrl getClusterManager() {
        return clusterManager;
    }

    public NomadJob setClusterManager(HttpUrl url) {
        this.clusterManager = url;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public NomadJob setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getVaultToken() {
        return vaultToken;
    }

    public void setVaultToken(String vaultToken) {
        this.vaultToken = vaultToken;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region != null ? region : "global";
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(final String datacenter) {
        this.datacenter = datacenter != null ? datacenter : "dc1";
    }

    public int getTaskHealthTimeoutMillis() {
        return taskHealthTimeout;
    }

    public NomadJob setTaskHealthTimeoutMillis(final int taskHealthTimeout) {
        this.taskHealthTimeout = taskHealthTimeout;
        return this;
    }

    public int getTaskHealthCheckPauseMillis() {
        return taskHealthCheckPause;
    }

    public NomadJob setTaskHealthCheckPauseMillis(final int taskHealthCheckPause) {
        this.taskHealthCheckPause = taskHealthCheckPause;
        return this;
    }

    public Job createJob() {
        Job nomadJob = new Job();
        nomadJob.setRegion(region);
        nomadJob.setId(groupId);
        nomadJob.setName(groupId);
        nomadJob.setType("service");
        nomadJob.addDatacenters(datacenter);
        nomadJob.setVaultToken("vault-token");
        for (Element element : getElements()) {
            if (element instanceof NomadTask) {
                nomadJob.addTaskGroups(((NomadTask) element).getTaskGroup());
            } else {
                throw new UnsupportedOperationException(
                    "Invalid Element type " + element.getClass().getName() + " in " + getClass().getName());
            }
        }
        return nomadJob;
    }

    @Override
    public CompletableFuture<Void> start(final Topology topology) {
        return CompletableFuture.runAsync(() -> {
            Job nomadJob = createJob();
            logger.info("Starting nomad job {} ...", nomadJob.getId());
            NomadApiClient client = NomadClientFactory.getClient(clusterManagerString());
            try {
                EvaluationResponse evaluationResponse = client.getJobsApi().register(nomadJob);

                waitForNomadJob(nomadJob.getId(), client, evaluationResponse);

                logger.info("Job {} started successfully", nomadJob.getId());

                final Map<String, String> taskIdToAllocationId = getJobTaskAllocationIds(client, nomadJob,
                    evaluationResponse.getValue());

                final Map<String, HostAndPort> hostsAndPorts = getHostsAndPorts(client, taskIdToAllocationId);

                for (Map.Entry<String, HostAndPort> entry : hostsAndPorts.entrySet()) {
                    final NomadTask nomadTask = topology.getResource(entry.getKey(), NomadTask.class);
                    nomadTask.setHostAndPort(entry.getValue());
                    if (nomadTask.isHttpEnabled()) {
                        nomadTask.setHttpUrl(HttpUrl.parse("http://" + entry.getValue().toString()));
                    }
                }

                final List<HealthCheck> healthChecks = getElements(HealthCheck.class);
                if (healthChecks.size() > 0) {
                    Executor executor = Executors.newFixedThreadPool(healthChecks.size());
                    CountDownLatch latch = new CountDownLatch(healthChecks.size());
                    final AtomicInteger healthCheckSuccesses = new AtomicInteger();

                    for (HealthCheck healthCheck : healthChecks) {
                        executor.execute(() -> {
                            final long startTime = System.currentTimeMillis();
                            final long timeout = startTime + getTaskHealthTimeoutMillis();
                            String id = healthCheck instanceof Resource ? ((Resource) healthCheck).getKey()
                                : healthCheck.getClass().getName();
                            while (System.currentTimeMillis() < timeout) {
                                if (healthCheck.isHealthy()) {
                                    healthCheckSuccesses.incrementAndGet();
                                    logger.info("'{}' resource healthy after {} millis", id,
                                        System.currentTimeMillis() - startTime);
                                    break;
                                } else {
                                    logger.info("Waiting a maximum of {} millis for '{}' resource to become healthy",
                                        getTaskHealthTimeoutMillis(), id);
                                    try {
                                        Thread.sleep(getTaskHealthCheckPauseMillis());
                                    } catch (InterruptedException ex) {
                                        logger.error("Interrupted", ex);
                                    }
                                }
                            }
                            latch.countDown();
                        });
                    }

                    latch.await(getTaskHealthTimeoutMillis() + 1000, TimeUnit.MILLISECONDS);
                    ((ExecutorService) executor).shutdown();

                    if (healthCheckSuccesses.get() < healthChecks.size()) {
                        logger.warn("{} out of {} resources failed to be healthy within {} millis",
                            (healthChecks.size() - healthCheckSuccesses.get()), healthChecks.size(),
                            getTaskHealthTimeoutMillis());
                    }
                }
            } catch (IOException | NomadException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> stop(final Topology topology) {
        return CompletableFuture.runAsync(() -> {
            NomadApiClient client = NomadClientFactory.getClient(clusterManagerString());
            try {
                client.getJobsApi().deregister(groupId);
            } catch (IOException | NomadException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
        });
    }

    private String clusterManagerString() {
        return StringUtils.stripEnd(clusterManager.toString(), "/");
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public void resolveVariables(Topology topology, final VariableResolver resolver) {
        this.groupId = resolver.resolve(groupId);
        for (Element element : getElements()) {
            topology.substituteVariables(element, resolver.clone());
        }
    }

    private void waitForNomadJob(String jobId, NomadApiClient client, EvaluationResponse evaluationResponse)
        throws InterruptedException {
        Thread.sleep(1000); // Wait for allocations to become available
        logger.debug("Waiting for all allocations for evaluation {} to report status 'RUNNING'",
            evaluationResponse.getValue());

        final String healthCheckMessage = "Evaluation " + evaluationResponse.getValue() + " for Nomad job " + jobId;
        HealthUtils.waitForHealth(JOB_HEALTH_CHECK_TIMEOUT, JOB_HEALTH_CHECK_PAUSE, healthCheckMessage, () -> {
                try {
                    return client.getEvaluationsApi()
                        .allocations(evaluationResponse.getValue()).getValue().stream()
                        .allMatch(a -> RUNNING.equalsIgnoreCase(a.getClientStatus()));
                } catch (IOException e) {
                    logger.error("Fatal error checking health for job {}", jobId, e);
                    throw new RuntimeException(e);
                } catch (NomadException e) {
                    logger.error("Fatal error checking health for job {}", jobId, e);
                    throw new RuntimeException(e);
                }
            });
        Thread.sleep(8000); // Once job reports running, it still takes time for port to become available
    }

    private Map<String, String> getJobTaskAllocationIds(NomadApiClient client, Job nomadJob, String evaluationId)
        throws NomadException,
        IOException {
        final List<String> taskGroupIds = nomadJob.getTaskGroups().stream().map(g -> g.getName()).collect(toList());

        final List<AllocationListStub> currentEvaluationAllocations = client.getEvaluationsApi().allocations
            (evaluationId).getValue();

        // If we submit the same job twice, nomad will create a new evaluation, but no new allocations for that
        // evaluation because it recognizes the job already exists and there is nothing to update. In this case, we
        // fetch the existing allocations
        final Evaluation evaluation = client.getJobsApi().evaluations(nomadJob.getId()).getValue().stream()
            .filter(e -> hasAllocations(client, e)).findFirst().orElseThrow(() -> new
                RuntimeException("No allocations found for job " + nomadJob.getId()));
        final List<AllocationListStub> mostRecentAllocations = client.getEvaluationsApi().allocations(evaluation
            .getId())
            .getValue();

        final List<AllocationListStub> allocations = currentEvaluationAllocations.isEmpty() ? mostRecentAllocations :
            currentEvaluationAllocations;

        final Map<String, String> taskIdToAllocationId = taskGroupIds.stream()
            .collect(toMap(identity(), id -> {
                final String allocationId = allocations.stream().filter(a -> {
                    final Map<String, TaskState> taskStates = a.getTaskStates();
                    if (taskStates == null) {
                        throw new NullPointerException("Task states is null. Job " + a.getJobId() + " may not have "
                            + "started properly.");
                    }
                    return taskStates.get(id) != null && RUNNING.equalsIgnoreCase(taskStates.get(id).getState());
                }).findFirst()
                    .orElseThrow(() -> new RuntimeException("Task " + id + " for job " + nomadJob.getId() + " does "
                        + "not exist."))
                    .getId();
                return allocationId;
            }));

        return taskIdToAllocationId;
    }

    private boolean hasAllocations(NomadApiClient client, Evaluation evaluation) {
        try {
            return !client.getEvaluationsApi().allocations(evaluation.getId()).getValue().isEmpty();
        } catch (NomadException | IOException e) {
            logger.error("Error fetching data from nomad API", e);
            return false;
        }
    }

    private Map<String, HostAndPort> getHostsAndPorts(NomadApiClient client, Map<String, String> allocationIdMap)
        throws NomadException, IOException {
        final Map<String, HostAndPort> hostAndPortMap = new HashMap<>();

        for (Map.Entry<String, String> entry : allocationIdMap.entrySet()) {
            final String taskId = entry.getKey();
            final String allocationId = entry.getValue();
            final String ip = client.getAllocationsApi().info(allocationId).getValue().getTaskResources()
                .get(taskId).getNetworks().get(0).getIp();
            final int port = client.getAllocationsApi().info(allocationId).getValue().getTaskResources().get
                (taskId).getNetworks().get(0).getDynamicPorts().get(0).getValue();

            final HostAndPort hostAndPort = HostAndPort.fromParts(ip, port);

            hostAndPortMap.put(taskId, hostAndPort);
        }
        return hostAndPortMap;
    }
}
