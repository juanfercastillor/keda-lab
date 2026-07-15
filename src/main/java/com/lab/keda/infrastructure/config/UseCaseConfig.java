package com.lab.keda.infrastructure.config;

import com.lab.keda.application.GetJobService;
import com.lab.keda.application.ProcessJobService;
import com.lab.keda.application.SubmitJobService;
import com.lab.keda.domain.port.in.GetJobUseCase;
import com.lab.keda.domain.port.in.ProcessJobUseCase;
import com.lab.keda.domain.port.in.SubmitJobUseCase;
import com.lab.keda.domain.port.out.JobQueuePort;
import com.lab.keda.domain.port.out.JobRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root: wires the framework-agnostic application services with the
 * infrastructure adapters. Keeping the wiring here lets the domain and
 * application layers stay free of any Spring annotation.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    SubmitJobUseCase submitJobUseCase(JobRepositoryPort repository, JobQueuePort queue) {
        return new SubmitJobService(repository, queue);
    }

    @Bean
    GetJobUseCase getJobUseCase(JobRepositoryPort repository) {
        return new GetJobService(repository);
    }

    @Bean
    ProcessJobUseCase processJobUseCase(
            JobRepositoryPort repository,
            JobQueuePort queue,
            @Value("${keda-lab.processing-cost-millis:750}") long processingCostMillis) {
        return new ProcessJobService(repository, queue, processingCostMillis);
    }
}
