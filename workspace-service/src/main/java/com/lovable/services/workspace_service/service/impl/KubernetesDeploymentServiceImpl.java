package com.lovable.services.workspace_service.service.impl;

import com.lovable.services.workspace_service.dto.project.DeployResponse;
import com.lovable.services.workspace_service.service.DeploymentService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesDeploymentServiceImpl implements DeploymentService {

    private final KubernetesClient client;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.preview.namespace}")
    private String namespace;

    @Value("${app.preview.domain}")
    private String baseDomain;

    @Value("${app.preview.proxy-port}")
    private String proxyPort;

    private static final String POOL_LABEL = "status";
    private static final String PROJECT_LABEL = "project-id";
    private static final String IDLE = "idle";
    private static final String BUSY = "busy";
    private static final String SYNCER_CONTAINER = "syncer";
    private static final String RUNNER_CONTAINER = "runner";

    @Override
    public DeployResponse deploy(Long projectId) {

        String domain = "project-" + projectId + baseDomain;

        Pod existingPod = findActivePod(projectId);

        if(existingPod != null) {
            registerRoute(domain, existingPod);
            return new DeployResponse("http://"+domain+":"+proxyPort);
        }

        return claimAndStartNewPod(projectId, domain);
    }

    @Override
    public String getPreviewUrl(Long projectId) {
        String domain = "project-" + projectId + baseDomain;
        return "http://" + domain + ":" + proxyPort;
    }

    @Override
    public void releasePod(Long projectId) {
        List<Pod> pods = client.pods().inNamespace(namespace)
                .withLabel(PROJECT_LABEL, projectId.toString())
                .list().getItems();

        for (Pod pod : pods) {
            String podName = pod.getMetadata().getName();
            log.info("Releasing pod {} for deleted project {}", podName, projectId);
            // Deleting (rather than relabeling back to idle) is simplest and safe: it drops
            // below the runner-pool Deployment's desired replica count, so a fresh clean
            // idle pod gets spun up automatically to replace it.
            client.pods().inNamespace(namespace).withName(podName).delete();
        }

        String domain = "project-" + projectId + baseDomain;
        redisTemplate.delete("route:" + domain);
    }

    private DeployResponse claimAndStartNewPod(Long projectId, String domain) {
        Pod pod = client.pods().inNamespace(namespace)
                .withLabel(POOL_LABEL, IDLE)
                .list().getItems().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No idle runners available. Please scale up the runner-pool."));

        String podName = pod.getMetadata().getName();
        log.info("Claiming pod {} for project {}", podName, projectId);

        client.pods().inNamespace(namespace).withName(podName).edit(p -> {
            p.getMetadata().getLabels().put(POOL_LABEL, BUSY);
            p.getMetadata().getLabels().put(PROJECT_LABEL, projectId.toString());
            return p;
        });

        try {
            // Syncer Commands
            String initialSyncCmd = String.format(
                    "mc mirror --overwrite myminio/projects/%d/ /app/",
                    projectId);

            log.info("Starting initial sync for project {} in pod {}", projectId, podName);
            execCommand(podName, SYNCER_CONTAINER, "sh", "-c", initialSyncCmd);

            String watchCmd = String.format(
                    "nohup mc mirror --overwrite --watch myminio/projects/%d/ /app/ > /app/sync.log 2>&1 &",
                    projectId);
            execCommand(podName, SYNCER_CONTAINER, "sh", "-c", watchCmd);

            // Runner Commands
            // Start the dev server in the background, then block (up to ~30s) until it is
            // actually accepting connections before we register the route / report success -
            // otherwise requests land during the npm install window and get ECONNREFUSED.
            String startCmd = "npm install && "
                    + "nohup npm run dev -- --host 0.0.0.0 --port 5173 > /app/dev.log 2>&1 & "
                    + "for i in $(seq 1 30); do wget -q -O /dev/null http://127.0.0.1:5173 && break; sleep 1; done";

            log.info("Starting dev server for project {}...", projectId);
            execCommand(podName, RUNNER_CONTAINER, "sh", "-c", startCmd);

            registerRoute(domain, pod);

            log.info("Deployment successful: http://{}:{}", domain, proxyPort);
            return new DeployResponse("http://" + domain + ":" + proxyPort);

        } catch(Exception e) {
            log.error("Deployment failed for project {}. Releasing pod {}.", projectId, podName, e);
            client.pods().inNamespace(namespace).withName(podName).delete();
            throw new RuntimeException("Failed to deploy the project with id: "+projectId);
        }
    }

    private void registerRoute(String domain, Pod pod) {
        String podIp = pod.getStatus().getPodIP();
        if (podIp == null) throw new RuntimeException("Pod is running but has no IP!");

        redisTemplate.opsForValue().set("route:" + domain, podIp + ":5173", 6, TimeUnit.HOURS);
    }


    private void execCommand(String podName, String container, String... command) {
        log.debug("Exec in {}:{} -> {}", podName, container, String.join(" ", command));

        CompletableFuture<String> data = new CompletableFuture<>();
        try (ExecWatch ignored = client.pods().inNamespace(namespace).withName(podName)
                .inContainer(container)
                .writingOutput(new ByteArrayOutputStream())
                .writingError(new ByteArrayOutputStream())
                .usingListener(new ExecListener() {
                    @Override
                    public void onClose(int code, String reason) {
                        data.complete("Done");
                    }
                })
                .exec(command)) {

            // Wait briefly to ensure command fired (Fabric8 exec is async)
            // For long running background jobs (nohup), we don't wait for "Done"
            if (command[command.length - 1].trim().endsWith("&")) {
                Thread.sleep(500);
            } else {
                data.get(30, TimeUnit.SECONDS); // Block for synchronous setup commands (npm install)
            }

        } catch (Exception e) {
            log.error("Exec failed", e);
            throw new RuntimeException("Pod Execution Failed", e);
        }
    }

    Pod findActivePod(Long projectId) {
        return client.pods().inNamespace(namespace)
                .withLabel(PROJECT_LABEL, projectId.toString())
                .withLabel(POOL_LABEL, BUSY) // Only find active/busy ones
                .list().getItems().stream()
                .filter(pod -> pod.getStatus().getPhase().equals("Running"))
                .findFirst()
                .orElse(null);
    }


}
