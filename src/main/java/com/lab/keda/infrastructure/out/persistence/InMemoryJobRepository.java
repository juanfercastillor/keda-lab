package com.lab.keda.infrastructure.out.persistence;

import com.lab.keda.domain.model.Job;
import com.lab.keda.domain.port.out.JobRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Output adapter: a simple thread-safe in-memory job store. Being a lab, it
 * avoids the operational weight of a real database while still exercising the
 * repository port. Swap this adapter for a JPA/Mongo one without touching the
 * domain or application layers.
 */
@Repository
public class InMemoryJobRepository implements JobRepositoryPort {

    private final ConcurrentMap<UUID, Job> store = new ConcurrentHashMap<>();

    @Override
    public Job save(Job job) {
        store.put(job.id(), job);
        return job;
    }

    @Override
    public Optional<Job> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public long count() {
        return store.size();
    }
}
