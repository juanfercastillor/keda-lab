package com.lab.keda.application;

import com.lab.keda.domain.model.Job;
import com.lab.keda.domain.port.in.GetJobUseCase;
import com.lab.keda.domain.port.out.JobRepositoryPort;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service that resolves a job by id.
 */
public class GetJobService implements GetJobUseCase {

    private final JobRepositoryPort repository;

    public GetJobService(JobRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public Optional<Job> findById(UUID id) {
        return repository.findById(id);
    }
}
