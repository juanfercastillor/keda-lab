package com.lab.keda.infrastructure.in.scheduler;

import com.lab.keda.domain.port.in.ProcessJobUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Input adapter: periodically drains the queue by invoking the processing use
 * case. Each replica runs its own poller, so adding replicas (via KEDA)
 * increases throughput and drains the backlog faster.
 */
@Component
public class ScheduledJobPoller {

    private final ProcessJobUseCase processJobUseCase;

    public ScheduledJobPoller(ProcessJobUseCase processJobUseCase) {
        this.processJobUseCase = processJobUseCase;
    }

    @Scheduled(fixedDelayString = "${keda-lab.poll-interval-millis:200}")
    public void pollOnce() {
        processJobUseCase.processNext();
    }
}
