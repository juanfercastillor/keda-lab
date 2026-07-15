package com.lab.keda.application;

import com.lab.keda.domain.model.Job;
import com.lab.keda.domain.port.in.ProcessJobUseCase;
import com.lab.keda.domain.port.out.JobQueuePort;
import com.lab.keda.domain.port.out.JobRepositoryPort;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service that consumes one job from the queue and processes it.
 * The processing cost is simulated so the queue builds up under load, which is
 * exactly the pressure KEDA reacts to when scaling replicas out.
 */
public class ProcessJobService implements ProcessJobUseCase {

    private final JobRepositoryPort repository;
    private final JobQueuePort queue;
    private final long processingCostMillis;

    public ProcessJobService(JobRepositoryPort repository, JobQueuePort queue, long processingCostMillis) {
        this.repository = Objects.requireNonNull(repository);
        this.queue = Objects.requireNonNull(queue);
        this.processingCostMillis = processingCostMillis;
    }

    @Override
    public Optional<UUID> processNext() {
        Optional<UUID> next = queue.poll();
        if (next.isEmpty()) {
            return Optional.empty();
        }
        UUID jobId = next.get();
        Optional<Job> maybeJob = repository.findById(jobId);
        if (maybeJob.isEmpty()) {
            return Optional.empty();
        }
        Job job = maybeJob.get();
        job.markProcessing();
        repository.save(job);
        try {
            simulateWork();
            job.markCompleted("processed:" + job.payload());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            job.markFailed("interrupted");
        }
        repository.save(job);
        return Optional.of(jobId);
    }

    private void simulateWork() throws InterruptedException {
        if (processingCostMillis > 0) {
            Thread.sleep(processingCostMillis);
        }
    }
}
