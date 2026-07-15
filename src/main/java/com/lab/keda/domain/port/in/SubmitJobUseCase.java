package com.lab.keda.domain.port.in;

import com.lab.keda.domain.model.Job;

/**
 * Input port: submit a new job for asynchronous processing.
 */
public interface SubmitJobUseCase {

    Job submit(SubmitJobCommand command);

    record SubmitJobCommand(String payload) {
    }
}
