package com.lab.keda.domain.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port: the queue that decouples job submission from job processing.
 * Its depth is the signal KEDA uses to scale the workload.
 */
public interface JobQueuePort {

    void enqueue(UUID jobId);

    Optional<UUID> poll();

    int depth();
}
