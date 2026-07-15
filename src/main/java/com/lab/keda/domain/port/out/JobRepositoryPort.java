package com.lab.keda.domain.port.out;

import com.lab.keda.domain.model.Job;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port: persistence abstraction for {@link Job} aggregates.
 */
public interface JobRepositoryPort {

    Job save(Job job);

    Optional<Job> findById(UUID id);

    long count();
}
