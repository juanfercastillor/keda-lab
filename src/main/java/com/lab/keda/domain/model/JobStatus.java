package com.lab.keda.domain.model;

/**
 * Lifecycle states of a {@link Job}.
 */
public enum JobStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
