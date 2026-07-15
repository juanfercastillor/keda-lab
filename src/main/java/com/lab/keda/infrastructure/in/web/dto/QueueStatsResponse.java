package com.lab.keda.infrastructure.in.web.dto;

/**
 * Web view of the current queue pressure. Handy to eyeball the same signal
 * KEDA scales on.
 */
public record QueueStatsResponse(int pendingJobs, long totalJobs) {
}
