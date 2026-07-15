package com.lab.keda.domain.port.in;

import java.util.Optional;
import java.util.UUID;

/**
 * Input port: process the next queued job, if any. Returns the id of the
 * processed job when work was performed, or empty when the queue was idle.
 */
public interface ProcessJobUseCase {

    Optional<UUID> processNext();
}
