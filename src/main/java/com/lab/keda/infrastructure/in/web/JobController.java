package com.lab.keda.infrastructure.in.web;

import com.lab.keda.domain.model.Job;
import com.lab.keda.domain.port.in.GetJobUseCase;
import com.lab.keda.domain.port.in.SubmitJobUseCase;
import com.lab.keda.domain.port.in.SubmitJobUseCase.SubmitJobCommand;
import com.lab.keda.domain.port.out.JobQueuePort;
import com.lab.keda.domain.port.out.JobRepositoryPort;
import com.lab.keda.infrastructure.in.web.dto.JobResponse;
import com.lab.keda.infrastructure.in.web.dto.QueueStatsResponse;
import com.lab.keda.infrastructure.in.web.dto.SubmitJobRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Input adapter: REST entry point to submit jobs, look them up and inspect the
 * queue pressure that KEDA autoscales on.
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final SubmitJobUseCase submitJobUseCase;
    private final GetJobUseCase getJobUseCase;
    private final JobQueuePort jobQueue;
    private final JobRepositoryPort jobRepository;

    public JobController(
            SubmitJobUseCase submitJobUseCase,
            GetJobUseCase getJobUseCase,
            JobQueuePort jobQueue,
            JobRepositoryPort jobRepository) {
        this.submitJobUseCase = submitJobUseCase;
        this.getJobUseCase = getJobUseCase;
        this.jobQueue = jobQueue;
        this.jobRepository = jobRepository;
    }

    @PostMapping
    public ResponseEntity<JobResponse> submit(@Valid @RequestBody SubmitJobRequest request) {
        Job job = submitJobUseCase.submit(new SubmitJobCommand(request.payload()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(job.id())
                .toUri();
        return ResponseEntity.created(location).body(JobResponse.from(job));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> get(@PathVariable UUID id) {
        return getJobUseCase.findById(id)
                .map(JobResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/stats")
    public QueueStatsResponse stats() {
        return new QueueStatsResponse(jobQueue.depth(), jobRepository.count());
    }
}
