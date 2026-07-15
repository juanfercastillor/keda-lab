package com.lab.keda.application;

import com.lab.keda.domain.model.Job;
import com.lab.keda.domain.model.JobStatus;
import com.lab.keda.domain.port.in.SubmitJobUseCase.SubmitJobCommand;
import com.lab.keda.domain.port.out.JobQueuePort;
import com.lab.keda.domain.port.out.JobRepositoryPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the submit -> queue -> process flow using lightweight in-test fakes,
 * so the application layer is exercised without Spring or infrastructure.
 */
class JobProcessingFlowTest {

    private JobRepositoryPort repository;
    private JobQueuePort queue;

    @BeforeEach
    void setUp() {
        repository = new FakeRepository();
        queue = new FakeQueue();
    }

    @Test
    void submitEnqueuesPendingJob() {
        SubmitJobService submit = new SubmitJobService(repository, queue);

        Job job = submit.submit(new SubmitJobCommand("hello"));

        assertThat(job.status()).isEqualTo(JobStatus.PENDING);
        assertThat(queue.depth()).isEqualTo(1);
        assertThat(repository.findById(job.id())).isPresent();
    }

    @Test
    void processNextCompletesTheQueuedJob() {
        SubmitJobService submit = new SubmitJobService(repository, queue);
        ProcessJobService process = new ProcessJobService(repository, queue, 0);

        Job job = submit.submit(new SubmitJobCommand("work"));

        Optional<UUID> processed = process.processNext();

        assertThat(processed).contains(job.id());
        assertThat(queue.depth()).isZero();
        Job stored = repository.findById(job.id()).orElseThrow();
        assertThat(stored.status()).isEqualTo(JobStatus.COMPLETED);
        assertThat(stored.result()).isEqualTo("processed:work");
    }

    @Test
    void processNextOnEmptyQueueDoesNothing() {
        ProcessJobService process = new ProcessJobService(repository, queue, 0);

        assertThat(process.processNext()).isEmpty();
    }

    private static final class FakeRepository implements JobRepositoryPort {
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

    private static final class FakeQueue implements JobQueuePort {
        private final ConcurrentLinkedQueue<UUID> queue = new ConcurrentLinkedQueue<>();

        @Override
        public void enqueue(UUID jobId) {
            queue.offer(jobId);
        }

        @Override
        public Optional<UUID> poll() {
            return Optional.ofNullable(queue.poll());
        }

        @Override
        public int depth() {
            return queue.size();
        }
    }
}
