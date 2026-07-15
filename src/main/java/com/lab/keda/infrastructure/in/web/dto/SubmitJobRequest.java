package com.lab.keda.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Web request body for submitting a job.
 */
public record SubmitJobRequest(
        @NotBlank(message = "payload must not be blank")
        @Size(max = 4096, message = "payload must be at most 4096 characters")
        String payload) {
}
