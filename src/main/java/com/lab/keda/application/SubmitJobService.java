package com.lab.keda.application;

import com.lab.keda.domain.model.Job;
import com.lab.keda.domain.port.in.SubmitJobUseCase;
import com.lab.keda.domain.port.out.JobQueuePort;
import com.lab.keda.domain.port.out.JobRepositoryPort;

import java.util.Objects;

/**
 * Application service orchestrating job submission: persist the job and push
 * its id onto the queue so a worker can pick it up later.
 */
public class SubmitJobService implements SubmitJobUseCase {

    private final JobRepositoryPort repository;
    private final JobQueuePort queue;

    public SubmitJobService(JobRepositoryPort repository, JobQueuePort queue) {
        this.repository = Objects.requireNonNull(repository);
        this.queue = Objects.requireNonNull(queue);
    }

    @Override
    public Job submit(SubmitJobCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Job job = Job.create(command.payload());
        Job saved = repository.save(job);
        queue.enqueue(saved.id());
        return saved;
    }
}
