package com.lab.keda.infrastructure.out.queue;

import com.lab.keda.domain.port.out.JobQueuePort;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Output adapter: an in-memory work queue whose depth is published as a
 * Micrometer gauge ({@code keda_lab_pending_jobs}). That gauge is scraped from
 * {@code /actuator/prometheus} and drives the KEDA Prometheus scaler.
 */
@Component
public class InMemoryJobQueue implements JobQueuePort {

    public static final String PENDING_JOBS_METRIC = "keda_lab_pending_jobs";

    private final BlockingQueue<UUID> queue = new LinkedBlockingQueue<>();

    public InMemoryJobQueue(MeterRegistry meterRegistry) {
        meterRegistry.gauge(PENDING_JOBS_METRIC, queue, BlockingQueue::size);
    }

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
