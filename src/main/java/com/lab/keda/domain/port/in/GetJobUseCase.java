package com.lab.keda.domain.port.in;

import com.lab.keda.domain.model.Job;

import java.util.Optional;
import java.util.UUID;

/**
 * Input port: query a job by its identifier.
 */
public interface GetJobUseCase {

    Optional<Job> findById(UUID id);
}
