package com.lab.keda.infrastructure.in.web.dto;

import com.lab.keda.domain.model.Job;

import java.time.Instant;
import java.util.UUID;

/**
 * Web view of a {@link Job}.
 */
public record JobResponse(
        UUID id,
        String payload,
        String status,
        Instant createdAt,
        Instant updatedAt,
        String result) {

    public static JobResponse from(Job job) {
        return new JobResponse(
                job.id(),
                job.payload(),
                job.status().name(),
                job.createdAt(),
                job.updatedAt(),
                job.result());
    }
}
