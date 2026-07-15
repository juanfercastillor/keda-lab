package com.lab.keda.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Core domain entity representing a unit of work that will be processed
 * asynchronously by a worker. This class is intentionally free of any
 * framework dependency so the domain stays isolated (clean architecture).
 */
public final class Job {

    private final UUID id;
    private final String payload;
    private JobStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private String result;

    private Job(UUID id, String payload, JobStatus status, Instant createdAt, Instant updatedAt, String result) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.payload = Objects.requireNonNull(payload, "payload must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.result = result;
    }

    /**
     * Factory for a brand new job in {@link JobStatus#PENDING} state.
     */
    public static Job create(String payload) {
        Instant now = Instant.now();
        return new Job(UUID.randomUUID(), payload, JobStatus.PENDING, now, now, null);
    }

    /**
     * Rehydrate an existing job (e.g. from a repository).
     */
    public static Job rehydrate(UUID id, String payload, JobStatus status, Instant createdAt, Instant updatedAt, String result) {
        return new Job(id, payload, status, createdAt, updatedAt, result);
    }

    public void markProcessing() {
        transitionTo(JobStatus.PROCESSING);
    }

    public void markCompleted(String result) {
        this.result = result;
        transitionTo(JobStatus.COMPLETED);
    }

    public void markFailed(String reason) {
        this.result = reason;
        transitionTo(JobStatus.FAILED);
    }

    private void transitionTo(JobStatus target) {
        this.status = target;
        this.updatedAt = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public String payload() {
        return payload;
    }

    public JobStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public String result() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Job job)) {
            return false;
        }
        return id.equals(job.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
